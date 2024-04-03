package vttp.project.app.backend.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Builder;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.Menu;
import vttp.project.app.backend.model.Order;
import vttp.project.app.backend.model.OrderEdit;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.model.Tax;
import vttp.project.app.backend.repository.StatsRepository;
import vttp.project.app.backend.repository.UserRepository;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecret;

    @Value("${stripe.public.key}")
    private String stripePublic;

    @Value("${stripe.webhook.endpoint}")
    private String stripeEp;

    @Value("${gst}")
    private Integer gst;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EmailService emailSvc;

    @Autowired
    private StatsRepository statsRepo;

    public JsonObject getKey() {
        return Json.createObjectBuilder().add("key", stripePublic).build();
    }

    public Long getLongAmount(Double amount) {
        amount *= 100;
        return Math.round(amount);
    }

    public Builder addLineItem(Builder builder, String name, Double price, Long quantity) {
        return builder.addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(quantity)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(name)
                                .build())
                        .setCurrency("sgd")
                        .setUnitAmount(getLongAmount(price))
                        .build())
                .build());
    }

    public Double applyTax(String id, Double amount) {

        Tax tax = userRepo.getTaxes(id);
        if (tax.getSvc() != 0)
            amount *= 100 + tax.getSvc();
        if (tax.getGst())
            amount *= 100 + gst;
        return amount / 10000;
    }

    public Builder applyTax(Builder builder, String id, Double amount) {
        Tax tax = userRepo.getTaxes(id);

        if (tax.getSvc() != 0) {
            Double svc = amount * tax.getSvc() / 100;
            amount += svc;
            builder = addLineItem(builder, "Service charge", svc, 1L);
        }
        if (tax.getGst()) {
            Double amt = amount * gst / 100;
            builder = addLineItem(builder, "GST", amt, 1L);
        }
        return builder;
    }

    public String createPaymentIntent(OrderRequest request, String success, String cancel) throws StripeException {

        Stripe.apiKey = stripeSecret;
        Builder builder = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(request.getEmail())
                .setSuccessUrl(success)
                .setCancelUrl(cancel);

        Double total = 0.0;
        for (Order order : request.getCart()) {
            Menu menu = userRepo.getMenu(order.getId());
            total += order.getQuantity() * menu.getPrice();
            builder = addLineItem(builder, menu.getName(), menu.getPrice(), order.getQuantity().longValue());
        }

        builder = applyTax(builder, request.getClient(), total);
        builder.putAllMetadata(request.toMetadata(total));
        return Session.create(builder.build()).getId();
    }

    public Event webhookAuth(String payload, String sig) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sig, stripeEp);
    }

    public OrderRequest handleEvent(Event event) {

        StripeObject stripe = null;
        switch (event.getType()) {
            case ("charge.succeeded"):
                stripe = event.getDataObjectDeserializer().getObject().get();
                Charge charge = (Charge) stripe;
                userRepo.saveCharge(new vttp.project.app.backend.model.Charge(charge.getId(), charge.getPaymentIntent(),
                        charge.getReceiptUrl()));
                break;

            case ("checkout.session.completed"):
                stripe = event.getDataObjectDeserializer().getObject().get();
                Session session = (Session) stripe;
                OrderRequest request = OrderRequest.fromMap(session.getMetadata());
                request.setPaymentId(session.getPaymentIntent());
                return request;

            case ("charge.refunded"):
                stripe = event.getDataObjectDeserializer().getObject().get();
                Charge refund = (Charge) stripe;

                try {
                    String email = userRepo.getEmail(refund.getPaymentIntent());
                    if (email == null)
                        email = statsRepo.getEmail(refund.getPaymentIntent()).getString("receipt");
                    emailSvc.sendReceipt(email, refund.getReceiptUrl());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            default:
                System.out.println("Unhandled event from stripe: %s".formatted(event.getType()));
                break;
        }
        return null;
    }

    public void createRefund(String id, OrderEdit edit) throws StripeException {

        Menu menu = userRepo.getMenu(edit.getItem());
        Double amount = applyTax(id, menu.getPrice() * (edit.getOld() - edit.getQuantity()));

        Stripe.apiKey = stripeSecret;
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(userRepo.getChargeId(edit.getId()))
                .setAmount(getLongAmount(amount))
                .build();
        Refund.create(params);
    }

    public String createRefund(String id) throws StripeException {

        Stripe.apiKey = stripeSecret;
        String charge = userRepo.getChargeId(id);

        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(charge)
                .build();
        Refund.create(params);

        return charge;
    }
}

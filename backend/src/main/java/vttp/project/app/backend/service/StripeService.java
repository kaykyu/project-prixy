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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.repository.UserRepository;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecret;

    @Value("${stripe.public.key}")
    private String stripePublic;

    @Value("${stripe.webhook.endpoint}")
    private String stripeEp;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EmailService emailSvc;

    public JsonObject getKey() {
        return Json.createObjectBuilder().add("key", stripePublic).build();
    }

    public Long getLongAmount(Double amount) {
        amount *= 100;
        return amount.longValue();
    }

    public String createPaymentIntent(OrderRequest request, String success, String cancel) throws StripeException {

        Stripe.apiKey = stripeSecret;
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(request.getEmail())
                .setSuccessUrl(success)
                .setCancelUrl(cancel)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(userRepo.getEstName(request.getClient()))
                                        .build())
                                .setCurrency("sgd")
                                .setUnitAmount(getLongAmount(request.getAmount()))
                                .build())
                        .build())
                .putAllMetadata(request.toMetadata())
                .build();
        return Session.create(params).getId();
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
                    emailSvc.sendReceipt(userRepo.getEmail(refund.getPaymentIntent()), refund.getReceiptUrl());
                } catch (IOException e) {
                    e.printStackTrace();
                };                
                break;

            default:
                System.out.println("Unhandled event from stripe: %s".formatted(event.getType()));
                break;
        }
        return null;
    }

    public void createRefund(String id) throws StripeException {

        Stripe.apiKey = stripeSecret;
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(id)
                .build();
        Refund.create(params);
    }

    public void createRefund(String id, Double price) throws StripeException {
        
        Stripe.apiKey = stripeSecret;
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(id)
                .setAmount(getLongAmount(price))
                .build();
        Refund.create(params);
    }
}

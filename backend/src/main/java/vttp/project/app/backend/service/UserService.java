package vttp.project.app.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.app.backend.exception.SaveOrderException;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.model.Tax;
import vttp.project.app.backend.repository.ClientRepository;
import vttp.project.app.backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private EmailService emailSvc;

    @Autowired
    private JwtDecoder decoder;

    @Value("${stripe.secret.key}")
    private String stripeSecret;

    @Value("${stripe.public.key}")
    private String stripePublic;

    @Value("${stripe.webhook.endpoint}")
    private String stripeEp;

    @Value("${webapp.host.url}")
    private String hostUrl;

    public Boolean getStatus(String id) {
        return clientRepo.getClient(id).getStatus();
    }

    public JsonArray getMenu(String id) {
        return Json.createArrayBuilder(clientRepo.getMenu(id).stream()
                .map(menu -> menu.toJson())
                .toList())
                .build();
    }

    public JsonObject getKey() {
        return Json.createObjectBuilder().add("key", stripePublic).build();
    }

    public JsonObject getTaxes(String id) {
        Tax tax = userRepo.getTaxes(id);
        if (tax == null)
            return JsonObject.EMPTY_JSON_OBJECT;
        return tax.toJson();
    }

    public JsonObject createPaymentSession(OrderRequest request, String token) throws StripeException {
        Stripe.apiKey = stripeSecret;
        String baseUrl = "%s/%s/%s".formatted(hostUrl, "order", token);
        Jwt jwt = decoder.decode(token);
        request.setClient(jwt.getSubject());
        request.setTable(jwt.getClaimAsString("table"));

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(request.getEmail())
                .setSuccessUrl("%s%s/%s".formatted(baseUrl, "/success", request.getId()))
                .setCancelUrl("%s%s".formatted(baseUrl, "/cart"))
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(request.getClient())
                                        .build())
                                .setCurrency("sgd")
                                .setUnitAmount(request.getLongAmount())
                                .build())
                        .build())
                .putAllMetadata(request.toMetadata())
                .build();

        Session session = Session.create(params);
        return Json.createObjectBuilder().add("id", session.getId()).build();
    }

    public void handleWebhookEvent(String payload, String sig) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sig, stripeEp);
        StripeObject stripe = null;

        switch (event.getType()) {
            case ("charge.succeeded"):
                stripe = event.getDataObjectDeserializer().getObject().get();
                Charge charge = (Charge) stripe;
                userRepo.saveReceipt(charge.getPaymentIntent(), charge.getReceiptUrl());
                break;

            case ("checkout.session.completed"):
                stripe = event.getDataObjectDeserializer().getObject().get();
                Session session = (Session) stripe;
                OrderRequest request = OrderRequest.fromMap(session.getMetadata());                
                request.setReceipt(session.getPaymentIntent());

                try {
                    saveOrder(request);
                } catch (SaveOrderException e) {
                    e.printStackTrace();
                }
                break;

            default:
                System.out.println("Unhandled event from stripe: " + event.getType());
        }
    }

    @Transactional(rollbackFor = SaveOrderException.class)
    public void saveOrder(OrderRequest request) throws SaveOrderException {
        if (!userRepo.saveOrder(request))
            throw new SaveOrderException("Failed to save into Orders");

        if (!userRepo.saveOrderItems(request))
            throw new SaveOrderException("Failed to save into OrderItems");
    }

    public JsonObject sendReceipt(String id) {
        OrderRequest obj = userRepo.getReceiptUrl(id);
        String url = userRepo.getReceipt(obj.getReceipt(), id);
        try {
            emailSvc.sendReceipt(obj.getEmail(), url);
            return Json.createObjectBuilder().add("email", obj.getEmail()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return JsonObject.EMPTY_JSON_OBJECT;
        }
    }

    public JsonArray getOrders(String id) {
        return Json.createArrayBuilder(userRepo.getOrderItems(id).stream()
                .map(value -> value.toJson())
                .toList()).build();
    }
}
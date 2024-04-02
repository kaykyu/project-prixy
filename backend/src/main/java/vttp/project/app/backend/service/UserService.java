package vttp.project.app.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.app.backend.exception.SqlOrdersException;
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
    private StripeService stripeSvc;

    @Autowired
    private EmailService emailSvc;

    @Autowired
    private JwtDecoder decoder;

    @Autowired
    private WebSocketHandler socket;

    @Value("${webapp.host.url}")
    private String hostUrl;

    public JsonArray getMenu(String id) {
        return Json.createArrayBuilder(clientRepo.getMenu(id).stream()
                .map(menu -> menu.toJson())
                .toList())
                .build();
    }

    public JsonObject getKey() {
        return stripeSvc.getKey();
    }

    public JsonObject getTaxes(String id) {

        Tax tax = userRepo.getTaxes(id);
        if (tax == null)
            return JsonObject.EMPTY_JSON_OBJECT;
        return tax.toJson();
    }

    public JsonObject newOrder(OrderRequest request, String token) throws StripeException {

        String baseUrl = "%s/%s/%s".formatted(hostUrl, "order", token);
        Jwt jwt = decoder.decode(token);
        request.setClient(jwt.getSubject());
        request.setTable(jwt.getClaimAsString("table"));

        return Json.createObjectBuilder()
                .add("id", stripeSvc.createPaymentIntent(
                        request,
                        "%s/success/%s".formatted(baseUrl, request.getId()),
                        "%s/cart".formatted(baseUrl)))
                .build();
    }

    public void incomingWebhook(String payload, String sig) throws SignatureVerificationException {

        OrderRequest request = stripeSvc.handleEvent(stripeSvc.webhookAuth(payload, sig));
        if (request != null)
            try {
                saveOrder(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public void saveOrder(OrderRequest request) throws SqlOrdersException, Exception {

        if (!userRepo.saveOrder(request))
            throw new SqlOrdersException("Failed to save into Orders");

        if (!userRepo.saveOrderItems(request))
            throw new SqlOrdersException("Failed to save into OrderItems");

        socket.clientOrderIn(request.getClient());
    }

    public JsonObject sendReceipt(String id) {
        
        String[] email = userRepo.getReceiptUrl(id);
        try {
            emailSvc.sendReceipt(email[0], email[1]);
            return Json.createObjectBuilder().add("email", email[0]).build();

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
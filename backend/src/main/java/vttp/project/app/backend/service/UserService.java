package vttp.project.app.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.app.backend.exception.SqlOrdersException;
import vttp.project.app.backend.model.LineItem;
import vttp.project.app.backend.model.Menu;
import vttp.project.app.backend.model.Order;
import vttp.project.app.backend.model.OrderDetails;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.model.OrderStatus;
import vttp.project.app.backend.model.Tax;
import vttp.project.app.backend.repository.ClientRepository;
import vttp.project.app.backend.repository.StatsRepository;
import vttp.project.app.backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private StatsRepository statsRepo;

    @Autowired
    private StripeService stripeSvc;

    @Autowired
    private EmailService emailSvc;

    @Autowired
    private WebSocketHandler socket;

    @Value("${webapp.host.url}")
    private String hostUrl;

    @Value("${gst}")
    private Integer gst;

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

    public JsonObject newPendingOrder(OrderRequest request) {

        request.setId(UUID.randomUUID().toString().substring(0, 7) + "c");
        List<LineItem> list = new ArrayList<>();

        for (Order order : request.getCart()) {
            Menu menu = userRepo.getMenu(order.getId());
            list.add(new LineItem(order.getName(), order.getQuantity(), menu.getPrice() * order.getQuantity()));
        }

        Tax tax = userRepo.getTaxes(request.getClient());
        if (tax.getSvc() > 0)
            list.add(new LineItem("Service Charge", LineItem.getTotal(list) * tax.getSvc() / 100));
        if (tax.getGst())
            list.add(new LineItem("GST", LineItem.getTotal(list) * gst / 100));

        userRepo.saveLineItems(request.getId(), list);
        request.setAmount(LineItem.getTotal(list));
        try {
            saveOrder(request, OrderStatus.PENDING, "");

        } catch (SqlOrdersException e) {
            e.printStackTrace();
            return JsonObject.EMPTY_JSON_OBJECT;
        }

        return Json.createObjectBuilder()
                .add("url", "/success/%s".formatted(request.getId())).build();
    }

    public JsonObject newStripeOrder(OrderRequest request) throws StripeException {

        request.setId(UUID.randomUUID().toString().substring(0, 7) + "s");
        return Json.createObjectBuilder()
                .add("id", stripeSvc.createPaymentIntent(
                        request,
                        "%s/success/%s".formatted(hostUrl, request.getId()),
                        "%s/order/%s/cart".formatted(hostUrl, request.getToken())))
                .build();
    }

    public void incomingWebhook(String payload, String sig) throws SignatureVerificationException {

        OrderRequest request = stripeSvc.handleEvent(stripeSvc.webhookAuth(payload, sig));
        if (request != null)
            try {
                saveOrder(request, OrderStatus.RECEIVED, "Order up!");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public void saveOrder(OrderRequest request, OrderStatus status, String message) throws SqlOrdersException {

        if (!userRepo.saveOrder(request, status))
            throw new SqlOrdersException("Failed to save into Orders");

        if (!userRepo.saveOrderItems(request))
            throw new SqlOrdersException("Failed to save into OrderItems");

        socket.sendSocketMessage(request.getClient(), message);
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

    public JsonObject getPostedOrders(String id) {

        OrderDetails order = userRepo.getOrderDetails(id);

        if (order == null) {
            if (statsRepo.checkOrderCompleted(id))
                return Json.createObjectBuilder().add("completed", true).build();
            return JsonObject.EMPTY_JSON_OBJECT;
        }
        
        order.setOrders(userRepo.getOrderItems(id));
        return order.toJson();
    }
}
package vttp.project.app.backend.service;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.stripe.exception.StripeException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.app.backend.exception.SqlOrdersException;
import vttp.project.app.backend.model.Client;
import vttp.project.app.backend.model.CompletedOrder;
import vttp.project.app.backend.model.Menu;
import vttp.project.app.backend.model.OrderEdit;
import vttp.project.app.backend.model.OrderStatus;
import vttp.project.app.backend.model.Tax;
import vttp.project.app.backend.repository.ClientRepository;
import vttp.project.app.backend.repository.S3Repository;
import vttp.project.app.backend.repository.StatsRepository;
import vttp.project.app.backend.repository.UserRepository;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private S3Repository s3Repo;

    @Autowired
    private StatsRepository statsRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtDecoder decoder;

    @Autowired
    private JwtTokenService jwtSvc;

    @Autowired
    private StripeService stripeSvc;

    @Autowired
    private WebSocketHandler socket;

    @Value("${gst}")
    private Integer gst;

    public String getId(String token) {
        return decoder.decode(token.split(" ")[1]).getSubject();
    }

    public String getEmail(String token) {
        return decoder.decode(token.split(" ")[1]).getClaimAsString("email");
    }

    public JsonObject getClient(String token) {
        Client client = clientRepo.getClient(getId(token));
        if (client == null)
            return JsonObject.EMPTY_JSON_OBJECT;
        return clientRepo.getClient(getId(token)).toJson();
    }

    public JsonObject putClient(String token, Client client) {
        clientRepo.putClient(getId(token), client);
        return getClient(token);
    }

    public JsonArray getMenu(String token) {
        return Json.createArrayBuilder(clientRepo.getMenu(getId(token)).stream()
                .map(menu -> menu.toJson())
                .toList())
                .build();
    }

    public JsonArray getMenuCategories(String token) {
        return Json.createArrayBuilder(clientRepo.getMenuCategories(getId(token))).build();
    }

    public Boolean saveMenu(Menu menu, MultipartFile imageFile, String token) {

        String key = UUID.randomUUID().toString().substring(0, 8);
        menu.setId(key);
        try {
            if (imageFile != null)
                menu.setImage(s3Repo.saveImage(imageFile, key));
            return clientRepo.saveMenu(menu, getId(token));

        } catch (IOException e) {
            return false;
        }
    }

    public Boolean putMenu(Menu menu, MultipartFile imageFile) {
        if (imageFile != null)
            try {
                menu.setImage(s3Repo.saveImage(imageFile, menu.getId()));
                clientRepo.putMenuImage(menu.getId(), menu.getImage());

            } catch (IOException e) {
                return false;
            }
        return clientRepo.putMenu(menu);
    }

    public Boolean deleteMenuImage(String id) {
        s3Repo.deleteImage(id);
        return clientRepo.putMenuImage(id, null);
    }

    public Boolean deleteMenu(String id) {
        return clientRepo.deleteMenu(id);
    }

    public JsonArray getKitchenOrders(String token) {
        return Json.createArrayBuilder(clientRepo.getKitchenOrders(getId(token)).stream()
                .map(doc -> doc.toJson())
                .toList())
                .build();
    }

    public JsonObject getOrderLink(String token, String table) {
        return jwtSvc.generateOrderLink(getId(token), table);
    }

    public JsonArray getBill(String order) {
        return Json.createArrayBuilder(clientRepo.getLineItems(order).stream()
                .map(value -> value.toJson())
                .toList())
                .build();
    }

    public Boolean postPayment(String token, String order) {
        Boolean result = clientRepo.updateOrderStatus(order, OrderStatus.RECEIVED);
        if (result)
            socket.sendSocketMessage(getId(token), "Order up!");
        return result;
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public void completeItem(String token, OrderEdit edit) throws SqlOrdersException {

        if (!(clientRepo.updateOrderItem(edit.getId(), edit.getItem(), true)
                && clientRepo.updateOrderProgress(edit.getProgress(), edit.getId())))
            throw new SqlOrdersException("Failed to update completed order item");

        socket.sendSocketMessage(getId(token), "");
    }

    public Double applyTax(String id, Double amount) {
        Tax tax = userRepo.getTaxes(id);

        if (tax.getSvc() != 0) {
            amount *= 100 + tax.getSvc();
            amount /= 100;
        }
        if (tax.getGst()) {
            amount *= 100 + gst;
            amount /= 100;
        }
        return amount;
    }

    public Double getAmount(String id, OrderEdit edit) {
        Menu menu = userRepo.getMenu(edit.getItem());
        return applyTax(id, menu.getPrice() * (edit.getOld() - edit.getQuantity()));
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public JsonObject editItem(String token, OrderEdit edit) throws SqlOrdersException {

        String id = getId(token);
        Double refund = getAmount(id, edit);
        Boolean updateAmt = clientRepo.updateOrderAmount(edit.getId(), refund);

        if (edit.getId().toLowerCase().endsWith("s"))
            try {
                stripeSvc.createRefund(userRepo.getChargeId(edit.getId()), refund);
                refund = 0.0;

            } catch (StripeException e) {
                return JsonObject.EMPTY_JSON_OBJECT;
            }

        if (!(clientRepo.updateOrderItem(edit.getId(), edit.getItem(), edit.getQuantity()) && updateAmt))
            throw new SqlOrdersException("Failed to update edited order item");

        socket.sendSocketMessage(getId(token), "OrderID: #%s has been changed".formatted(edit.getId().toUpperCase()));

        DecimalFormat df = new DecimalFormat("0.00");
        return Json.createObjectBuilder().add("refund", df.format(refund)).build();
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public JsonObject deleteItem(String token, OrderEdit edit) throws SqlOrdersException {

        String id = getId(token);
        Double refund = getAmount(id, edit);
        Boolean updateAmt = clientRepo.updateOrderAmount(edit.getId(), refund);

        if (edit.getId().toLowerCase().endsWith("s"))
            try {
                stripeSvc.createRefund(userRepo.getChargeId(edit.getId()), refund);
                refund = 0.0;

            } catch (StripeException e) {
                return JsonObject.EMPTY_JSON_OBJECT;
            }

        switch (edit.getProgress()) {
            case 100:
                if (!(clientRepo.removeOrderItem(edit.getId(), edit.getItem()) && completeOrder(edit.getId())
                        && updateAmt))
                    throw new SqlOrdersException("Failed to update removed order item");
                break;

            default:
                if (!(clientRepo.updateOrderProgress(edit.getProgress(), edit.getId())
                        && clientRepo.removeOrderItem(edit.getId(), edit.getItem()) && updateAmt))
                    throw new SqlOrdersException("Failed to update removed order item");
        }

        socket.sendSocketMessage(getId(token), "OrderID: #%s has been changed".formatted(edit.getId().toUpperCase()));

        DecimalFormat df = new DecimalFormat("0.00");
        return Json.createObjectBuilder().add("refund", df.format(refund)).build();
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public Boolean completeOrder(String id) throws SqlOrdersException {

        CompletedOrder order = clientRepo.getOrder(id);
        if (order == null)
            return false;

        order = clientRepo.getOrderItems(order);

        if (id.toLowerCase().endsWith("s"))
            if (!clientRepo.removeCharge(order.getCharge()))
                throw new SqlOrdersException("Failed to update removed charge");

        if (!(clientRepo.removeOrderItems(id) && clientRepo.removeOrder(id)))
            throw new SqlOrdersException("Failed to update completed order");

        statsRepo.saveCompletedOrder(order);
        socket.sendSocketMessage(order.getClientId(), "OrderID: #%s has been completed".formatted(id.toUpperCase()));
        return true;
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public JsonObject removeOrder(String token, String id) throws SqlOrdersException {

        Double refund = 0.0;
        if (id.endsWith("c"))
            refund = clientRepo.getOrder(id).getAmount();

        else {
            try {
                if (!clientRepo.removeCharge(stripeSvc.createRefund(id)))
                    throw new SqlOrdersException("Failed to update removed charge");

            } catch (StripeException e) {
                return JsonObject.EMPTY_JSON_OBJECT;
            }
        }

        if (!(clientRepo.removeOrderItems(id) && clientRepo.removeOrder(id)))
            throw new SqlOrdersException("Failed to update removed order");

        socket.sendSocketMessage(getId(token), "OrderID: #%s has been removed".formatted(id.toUpperCase()));

        DecimalFormat df = new DecimalFormat("0.00");
        return Json.createObjectBuilder().add("refund", df.format(refund)).build();
    }

    public String getStats(String token, Integer q) {
        try {
            return statsRepo.getAll(getId(token), q).toJson();
        } catch (Exception e) {
            return null;
        }
    }

    public JsonObject createCSV(String token, Integer q) throws IOException {
        StringWriter sw = new StringWriter();
        List<Document> docs = statsRepo.getRecords(getId(token), q);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("order_id", "date", "table", "email", "name", "comments", "payment_id", "charge_id",
                        "receipt", "amount", "orders")
                .build();

        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            docs.forEach(doc -> {
                JsonArray orders = Json.createArrayBuilder(doc.getList("orders", Document.class).stream()
                        .map(order -> Json.createObjectBuilder()
                                .add(order.getString("name"), order.getInteger("quantity")).build())
                        .toList())
                        .build();
                try {
                    printer.printRecord(
                            doc.getString("id"),
                            doc.getString("date"),
                            doc.getString("table_id"),
                            doc.getString("email"),
                            doc.getString("name"),
                            doc.getString("comments"),
                            doc.getString("payment_id"),
                            doc.getString("charge_id"),
                            doc.getString("receipt"),
                            doc.getDouble("amount"),
                            orders.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return Json.createObjectBuilder().add("csv", sw.toString()).build();
    }
}

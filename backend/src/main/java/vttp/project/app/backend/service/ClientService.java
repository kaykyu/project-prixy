package vttp.project.app.backend.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import vttp.project.app.backend.repository.ClientRepository;
import vttp.project.app.backend.repository.S3Repository;
import vttp.project.app.backend.repository.StatsRepository;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private S3Repository s3Repo;

    @Autowired
    private StatsRepository statsRepo;

    @Autowired
    private JwtDecoder decoder;

    @Autowired
    private JwtTokenService jwtSvc;

    @Autowired
    private StripeService stripeSvc;

    public String getId(String token) {
        return decoder.decode(token.split(" ")[1]).getSubject();
    }

    public JsonObject getClient(String token) {
        return clientRepo.getClient(getId(token)).toJson();
    }

    public JsonObject putClient(String token, Client client) {
        clientRepo.putClient(getId(token), client);
        return getClient(token);
    }

    public JsonObject putEmail(String token, String email) {
        try {
            clientRepo.putEmail(getId(token), email);
            return JsonObject.EMPTY_JSON_OBJECT;
        } catch (DuplicateKeyException e) {
            return Json.createObjectBuilder().add("error", "Email has already been used.").build();
        }
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
        try {
            if (imageFile != null)
                menu.setImage(s3Repo.saveImage(imageFile, menu.getId()));
            return clientRepo.putMenu(menu);

        } catch (IOException e) {
            return false;
        }
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

    @Transactional(rollbackFor = SqlOrdersException.class)
    public void completeItem(OrderEdit edit) throws SqlOrdersException {

        if (!(clientRepo.updateOrderItem(edit.getId(), edit.getItem(), true)
                && clientRepo.updateOrderProgress(edit.getProgress(), edit.getId())))
            throw new SqlOrdersException("Failed to update completed order item");
    }

    public Boolean editItem(String token, OrderEdit edit) {

        try {
            stripeSvc.createRefund(getId(token), edit);
            return clientRepo.updateOrderItem(edit.getId(), edit.getItem(), edit.getQuantity());

        } catch (StripeException e) {
            return false;
        }
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public Boolean deleteItem(String token, OrderEdit edit) throws SqlOrdersException {
        try {
            stripeSvc.createRefund(getId(token), edit);
            if (edit.getProgress() == 100) {
                clientRepo.removeOrderItem(edit.getId(), edit.getItem());
                completeOrder(edit.getId());
                return true;

            } else if ((clientRepo.updateOrderProgress(edit.getProgress(), edit.getId())
                    && clientRepo.removeOrderItem(edit.getId(), edit.getItem())))
                return true;
            else
                throw new SqlOrdersException("Failed to update removed order item");

        } catch (StripeException e) {
            return false;
        }
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public Boolean completeOrder(String id) throws SqlOrdersException {

        CompletedOrder order = clientRepo.getOrder(id);
        if (order == null)
            return false;

        order = clientRepo.getOrderItems(order);

        if (!(clientRepo.removeOrderItems(id) && clientRepo.removeOrder(id)
                && clientRepo.removeCharge(order.getCharge())))
            throw new SqlOrdersException("Failed to update completed order");

        statsRepo.saveCompletedOrder(order);
        return true;
    }

    @Transactional(rollbackFor = SqlOrdersException.class)
    public Boolean removeOrder(String id) throws SqlOrdersException {

        try {
            String charge = stripeSvc.createRefund(id);
            if (!(clientRepo.removeCharge(charge) && clientRepo.removeOrderItems(id)
                    && clientRepo.removeOrder(id)))
                throw new SqlOrdersException("Failed to update removed order");
            return true;

        } catch (StripeException e) {
            return false;
        }
    }

    public String getStats(String token, Integer q) {
        try {
            return statsRepo.getAll(getId(token), q).toJson();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonObject createCSV(String token, Integer q) throws IOException {
        StringWriter sw = new StringWriter();
        List<Document> docs = statsRepo.getRecords(getId(token), q);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("id", "client_id", "date", "table", "email", "name", "comments", "payment_id", "charge_id",
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
                            doc.getString("client_id"),
                            doc.getDate("ordered_date").toString(),
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

package vttp.project.app.backend.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.app.backend.exception.CompleteOrderException;
import vttp.project.app.backend.model.Client;
import vttp.project.app.backend.model.CompletedOrder;
import vttp.project.app.backend.model.Menu;
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
                .map(value -> value.toJson())
                .toList())
                .build();
    }

    public JsonObject getOrderLink(String token, String table) {
        return jwtSvc.generateOrderLink(getId(token), table);
    }

    @Transactional(rollbackFor = CompleteOrderException.class)
    public Boolean completeOrder(String id) throws CompleteOrderException {
        CompletedOrder order = clientRepo.getOrder(id);
        if (order == null)
            return false;

        order = clientRepo.getOrderItems(order);
        if (order.getOrders() == null)
            return false;

        if (!(clientRepo.removeOrderItems(id) && clientRepo.removeOrder(id)))
            throw new CompleteOrderException("Failed to delete completed order");

        statsRepo.saveCompletedOrder(order);
        return true;
    }

    public Boolean toggleKitchenStatus(String token, Boolean status) {
        return clientRepo.toggleKitchenStatus(getId(token), status);
    }

    public String getStats(String token, Integer q) {
        try {
            return statsRepo.getAll(getId(token), q).toJson();
        } catch (Exception e) {
            return null;
        }
    }
}

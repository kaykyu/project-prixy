package vttp.project.app.backend.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.JsonObject;
import vttp.project.app.backend.exception.SqlOrdersException;
import vttp.project.app.backend.model.Client;
import vttp.project.app.backend.model.Menu;
import vttp.project.app.backend.model.MenuCategory;
import vttp.project.app.backend.model.OrderEdit;
import vttp.project.app.backend.service.ClientService;

@RestController
@RequestMapping(path = "/api/client")
public class ClientController {

    @Autowired
    private ClientService clientSvc;

    @GetMapping()
    public ResponseEntity<String> getClient(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(clientSvc.getClient(token).toString());
    }

    @PutMapping()
    public ResponseEntity<String> putClient(@RequestHeader("Authorization") String token, @RequestBody Client client) {
        return ResponseEntity.ok(clientSvc.putClient(token, client).toString());
    }

    @PutMapping(path = "/email")
    public ResponseEntity<String> putEmail(@RequestHeader("Authorization") String token, @RequestBody String email) {

        JsonObject result = clientSvc.putEmail(token, email);
        if (result.isEmpty())
            return ResponseEntity.ok(email);
        return ResponseEntity.badRequest().body(result.toString());
    }

    @GetMapping(path = "/menu")
    public ResponseEntity<String> getMenu(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(clientSvc.getMenu(token).toString());
    }

    @GetMapping(path = "/menu/categories")
    public ResponseEntity<String> getCategories(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(clientSvc.getMenuCategories(token).toString());
    }

    @PostMapping(path = "/menu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postMenu(@RequestHeader("Authorization") String token, @RequestPart String name,
            @RequestPart(required = false) String description, @RequestPart String price,
            @RequestPart String category, @RequestPart(required = false) MultipartFile image) {

        Menu menu = new Menu(name, description, Double.parseDouble(price), MenuCategory.valueOf(category));
        if (clientSvc.saveMenu(menu, image, token))
            return ResponseEntity.status(HttpStatusCode.valueOf(201)).build();
        return ResponseEntity.internalServerError().build();
    }

    @PutMapping(path = "/menu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> putMenu(@RequestPart String id, @RequestPart String name,
            @RequestPart(required = false) String description, @RequestPart String price,
            @RequestPart String category, @RequestPart(required = false) MultipartFile image) {

        Menu menu = new Menu(id, name, description, null, Double.parseDouble(price), MenuCategory.valueOf(category));
        if (clientSvc.putMenu(menu, image))
            return ResponseEntity.ok().build();
        return ResponseEntity.internalServerError().build();
    }

    @DeleteMapping(path = "/menu/{id}/image")
    public ResponseEntity<Void> deleteMenuImage(@PathVariable String id) {
        if (clientSvc.deleteMenuImage(id))
            return ResponseEntity.ok().build();
        return ResponseEntity.internalServerError().build();
    }

    @DeleteMapping(path = "/menu/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable String id) {
        if (clientSvc.deleteMenu(id))
            return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/kitchen")
    public ResponseEntity<String> getKitchenOrders(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(clientSvc.getKitchenOrders(token).toString());
    }

    @GetMapping(path = "/orderLink")
    public ResponseEntity<String> getOrderLink(@RequestHeader("Authorization") String token,
            @RequestParam String table) {
        return ResponseEntity.ok(clientSvc.getOrderLink(token, table).toString());
    }

    @GetMapping(path = "/bill")
    public ResponseEntity<String> getBill(@RequestParam String order) {
        return ResponseEntity.ok(clientSvc.getBill(order).toString());
    }

    @PostMapping(path = "/payment")
    public ResponseEntity<Void> postPayment(@RequestHeader("Authorization") String token, @RequestBody String order) {
        if (clientSvc.postPayment(token, order))
            return ResponseEntity.ok().build();
        return ResponseEntity.internalServerError().build();
    }

    @PostMapping(path = "/order/item")
    public ResponseEntity<String> completeItem(@RequestHeader("Authorization") String token, @RequestBody OrderEdit edit) {
        try {
            clientSvc.completeItem(token, edit);
            return ResponseEntity.ok().build();

        } catch (SqlOrdersException e) {
            return ResponseEntity.internalServerError().body(e.toJson().toString());
        }
    }

    @PutMapping(path = "/order/item")
    public ResponseEntity<String> putItem(@RequestHeader("Authorization") String token, @RequestBody OrderEdit edit) {
        JsonObject result = clientSvc.editItem(token, edit);
        if (result.isEmpty())
            return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(result.toString());
    }

    @PostMapping(path = "/order/item/delete")
    public ResponseEntity<String> deleteItem(@RequestHeader("Authorization") String token,
            @RequestBody OrderEdit edit) {
        try {
            JsonObject result = clientSvc.deleteItem(token, edit);
            if (result.isEmpty())
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(result.toString());

        } catch (SqlOrdersException e) {
            return ResponseEntity.internalServerError().body(e.toJson().toString());
        }
    }

    @PostMapping(path = "/order/complete")
    public ResponseEntity<String> completeOrder(@RequestBody String id) {
        try {
            if (clientSvc.completeOrder(id))
                return ResponseEntity.ok().build();
            return ResponseEntity.notFound().build();
        } catch (SqlOrdersException e) {
            return ResponseEntity.internalServerError().body(e.toJson().toString());
        }
    }

    @PostMapping(path = "/order/delete")
    public ResponseEntity<String> deleteOrder(@RequestHeader("Authorization") String token, @RequestBody String id) {
        try {
            JsonObject result = clientSvc.removeOrder(token, id);
            if (result.isEmpty())
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(result.toString());
            
        } catch (SqlOrdersException e) {
            return ResponseEntity.internalServerError().body(e.toJson().toString());
        }
    }

    @GetMapping(path = "/stats")
    public ResponseEntity<String> getStats(@RequestHeader("Authorization") String token, @RequestParam Integer q) {
        String result = clientSvc.getStats(token, q);
        if (result == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping(path = "/records")
    public ResponseEntity<String> getRecords(@RequestHeader("Authorization") String token, @RequestParam Integer q) {
        try {
            return ResponseEntity.ok(clientSvc.createCSV(token, q).toString());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

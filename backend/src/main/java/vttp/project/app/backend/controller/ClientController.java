package vttp.project.app.backend.controller;

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
import vttp.project.app.backend.exception.CompleteOrderException;
import vttp.project.app.backend.model.Client;
import vttp.project.app.backend.model.Menu;
import vttp.project.app.backend.model.MenuCategory;
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

    @PostMapping(path = "/complete")
    public ResponseEntity<Void> completeOrder(@RequestBody String id) {
        try {
            if (clientSvc.completeOrder(id))
                return ResponseEntity.ok().build();
            return ResponseEntity.notFound().build();
        } catch (CompleteOrderException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(path = "/kitchen/status")
    public ResponseEntity<Void> postKitchenStatus(@RequestHeader("Authorization") String token,
            @RequestBody Boolean status) {
        if (clientSvc.toggleKitchenStatus(token, status))
            return ResponseEntity.ok().build();
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping(path = "/stats")
    public ResponseEntity<String> getStats(@RequestHeader("Authorization") String token, @RequestParam Integer q) {
        String result = clientSvc.getStats(token, q);
        if (result == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}

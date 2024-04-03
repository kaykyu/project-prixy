package vttp.project.app.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.service.UserService;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {

    @Autowired
    private UserService userSvc;

    @GetMapping(path = "/{id}/menu")
    public ResponseEntity<String> getMenu(@PathVariable String id) {
        return ResponseEntity.ok().body(userSvc.getMenu(id).toString());
    }

    @GetMapping(path = "/key")
    public ResponseEntity<String> getKey() {
        return ResponseEntity.ok(userSvc.getKey().toString());
    }

    @GetMapping(path = "/{id}/tax")
    public ResponseEntity<String> getTaxes(@PathVariable String id) {

        JsonObject result = userSvc.getTaxes(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.toString());
    }

    @PostMapping(path = "/order")
    public ResponseEntity<String> postOrder(@RequestBody OrderRequest request) {
        try {
            return ResponseEntity.ok()
                    .body(userSvc.newOrder(request).toString());

        } catch (StripeException e) {
            return ResponseEntity.internalServerError()
                    .body(Json.createObjectBuilder().add("error", e.getMessage()).build().toString());
        }
    }

    @PostMapping(path = "/webhook")
    public ResponseEntity<Void> postWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sig) {
        try {
            userSvc.incomingWebhook(payload, sig);
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/receipt")
    public ResponseEntity<String> postReceipt(@RequestBody String payload) {

        JsonObject result = userSvc.sendReceipt(payload);
        if (result.isEmpty())
            return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(path = "/orders/{id}")
    public ResponseEntity<String> getOrders(@PathVariable String id) {

        JsonArray result = userSvc.getOrders(id);
        if (result.size() < 1)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.toString());
    }
}
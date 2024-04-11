package vttp.project.app.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;

import vttp.project.app.backend.model.TeleUpdate;
import vttp.project.app.backend.service.TelegramService;
import vttp.project.app.backend.service.UserService;

@RestController
@RequestMapping(path = "/webhook")
public class WebhookController {

    @Autowired
    private UserService userSvc;

    @Autowired
    private TelegramService teleSvc;

    @PostMapping(path = "/stripe")
    public ResponseEntity<Void> postStripe(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sig) {
        try {
            userSvc.incomingWebhook(payload, sig);
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/telegram")
    public ResponseEntity<Void> postTele(@RequestBody String payload) {
        teleSvc.handleTeleUpdate(TeleUpdate.fromPayload(payload));
        return ResponseEntity.ok().build();
    }
}

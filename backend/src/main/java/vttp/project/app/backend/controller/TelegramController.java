package vttp.project.app.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vttp.project.app.backend.model.TeleUpdate;
import vttp.project.app.backend.service.TelegramService;

@RestController
@RequestMapping(path = "/telegram")
public class TelegramController {

    @Autowired
    private TelegramService teleSvc;
    
     @PostMapping(path = "/webhook")
    public ResponseEntity<Void> postTele(@RequestBody String payload) {
        if (teleSvc.handleTeleUpdate(TeleUpdate.fromPayload(payload)))
            return ResponseEntity.ok().build();
        return ResponseEntity.internalServerError().build();
    }
}

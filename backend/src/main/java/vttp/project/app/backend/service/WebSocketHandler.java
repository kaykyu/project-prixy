package vttp.project.app.backend.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.json.Json;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private Map<WebSocketSession, String> clients;
    private Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            String id = Json.createReader(new StringReader((String) message.getPayload())).readObject()
                    .getString("client");
            logger.info("%s connected".formatted(id));
            clients.put(session, id);
        } catch (Exception e) {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {

        if (clients == null)
            clients = new HashMap<WebSocketSession, String>();
        logger.info("Connection establisted");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        clients.remove(session);
    }

    public void clientOrderIn(String id) {
        clients.forEach((key, value) -> {
            if (value.equals(id))
                try {
                    key.sendMessage(new TextMessage(""));
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });        
    }
}
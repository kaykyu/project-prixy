package vttp.project.app.backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebSocketHandler extends TextWebSocketHandler {

    private List<WebSocketSession> clients;
    private Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        logger.info("Processing message from Websocket");
        String[] sessUrl = session.getUri().toString().split("/");
        String id = sessUrl[sessUrl.length - 1];

        clients.stream()
                .filter(sess -> {
                    String[] url = sess.getUri().toString().split("/");
                    return url[url.length - 1].equals(id);
                })
                .forEach(sess -> {
                    try {
                        sess.sendMessage(new TextMessage(""));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (clients == null)
            clients = new ArrayList<>();
        clients.add(session);
        logger.info("Connection establisted: %s".formatted(session.getUri()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        clients.remove(session);
        logger.info("Connection closed: %s".formatted(session.getUri()));
    }
}
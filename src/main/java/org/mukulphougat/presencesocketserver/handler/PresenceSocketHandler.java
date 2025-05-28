package org.mukulphougat.presencesocketserver.handler;

import lombok.extern.slf4j.Slf4j;
import org.mukulphougat.presencesocketserver.service.JwtService;
import org.mukulphougat.presencesocketserver.service.RedisPresenceService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PresenceSocketHandler extends TextWebSocketHandler {
    private final JwtService jwtService;
    private final RedisPresenceService redisPresenceService;

    // Track active sessions if needed
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public PresenceSocketHandler(JwtService jwtService, RedisPresenceService redisPresenceService) {
        this.jwtService = jwtService;
        this.redisPresenceService = redisPresenceService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromQuery(session);
        if (token == null) {
            log.warn("Missing token, closing connection");
            session.close();
            return;
        }

        try {
            String userId = jwtService.extractUserId(token);
            redisPresenceService.markUserOnline(userId);
            sessions.put(userId, session);

            log.info("User {} connected via WebSocket", userId);
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String token = getTokenFromQuery(session);
        try {
            String userId = jwtService.extractUserId(token);
            redisPresenceService.markUserOffline(userId);
            sessions.remove(userId);

            log.info("User {} disconnected", userId);
        } catch (Exception e) {
            log.error("Error cleaning up session: {}", e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error: {}", exception.getMessage());
    }

    private String getTokenFromQuery(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] parts = param.split("=");
            if (parts.length == 2 && parts[0].equals("token")) {
                return parts[1];
            }
        }
        return null;
    }
}

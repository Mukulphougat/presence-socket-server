package org.mukulphougat.presencesocketserver.handler;

import lombok.extern.slf4j.Slf4j;
import org.mukulphougat.presencesocketserver.constants.PresenceStatus;
import org.mukulphougat.presencesocketserver.dto.UserActivityLog;
import org.mukulphougat.presencesocketserver.service.JwtService;
import org.mukulphougat.presencesocketserver.service.OutboxService;
import org.mukulphougat.presencesocketserver.service.RedisPresenceService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PresenceSocketHandler extends TextWebSocketHandler {
    private final OutboxService outboxService;
    private final JwtService jwtService;
    private final RedisPresenceService redisPresenceService;

    // Track active sessions if needed
    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public PresenceSocketHandler(JwtService jwtService, OutboxService outboxService, RedisPresenceService redisPresenceService) {
        this.jwtService = jwtService;
        this.outboxService=outboxService;
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
            String sessionId = session.getId();

            session.getAttributes().put("userId", userId);
            session.getAttributes().put("sessionId", sessionId);

            redisPresenceService.markSessionOnline(userId, sessionId);

            // Emit USER_ONLINE only if this is the first session (presence key didn't exist before)
            if (redisPresenceService.isUserOnline(userId) && redisPresenceService.markSessionOffline(userId, sessionId)) {
                redisPresenceService.markSessionOnline(userId, sessionId); // re-add after check

                UserActivityLog userActivityLog = new UserActivityLog();
                userActivityLog.setUserId(userId);
                userActivityLog.setStatus(PresenceStatus.ONLINE);
                userActivityLog.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                outboxService.saveEvent("USER_ONLINE", userId, userActivityLog);
                log.info("User {} connected via WebSocket", userId);
            }
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            String userId = (String) session.getAttributes().get("userId");
            String sessionId = (String) session.getAttributes().get("sessionId");

            if (userId == null || sessionId == null) return;

            boolean isNowOffline = redisPresenceService.markSessionOffline(userId, sessionId);

            if (isNowOffline) {
                UserActivityLog userActivityLog = new UserActivityLog();
                userActivityLog.setUserId(userId);
                userActivityLog.setStatus(PresenceStatus.OFFLINE);
                userActivityLog.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                outboxService.saveEvent("USER_OFFLINE", userId, userActivityLog);
                log.info("User {} disconnected (last session)", userId);
            } else {
                log.info("User {} session {} disconnected, other sessions still active", userId, sessionId);
            }
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

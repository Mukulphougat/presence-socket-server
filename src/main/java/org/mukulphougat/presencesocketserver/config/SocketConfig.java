package org.mukulphougat.presencesocketserver.config;

import org.mukulphougat.presencesocketserver.handler.PresenceSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SocketConfig implements WebSocketConfigurer {

    @Autowired
    private final PresenceSocketHandler presenceSocketHandler;
    SocketConfig(PresenceSocketHandler presenceSocketHandler) {
        this.presenceSocketHandler = presenceSocketHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(presenceSocketHandler, "/ws/presence")
                .setAllowedOrigins("*"); // Use proper CORS for prod
//                .withSockJS(); // Optional: remove if using native WebSocket
    }
}
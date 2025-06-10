package org.mukulphougat.presencesocketserver.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisPresenceService {

    private final StringRedisTemplate redisTemplate;
    // In-memory set of online users
//    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    public RedisPresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

//    public void markUserOnline(String userId) {
//        redisTemplate.opsForValue().set("presence:user:" + userId, "online", Duration.ofSeconds(60));
//        onlineUsers.add(userId);
//    }
//
//    public void markUserOffline(String userId) {
//        redisTemplate.delete("presence:user:" + userId);
//        onlineUsers.remove(userId);
//    }

    // Add session and set user as online
    public void markSessionOnline(String userId, String sessionId) {
        String sessionKey = "presence:user:" + userId + ":sessions";
        redisTemplate.opsForSet().add(sessionKey, sessionId);
        redisTemplate.expire(sessionKey, Duration.ofMinutes(5));

        String presenceKey = "presence:user:" + userId;
        redisTemplate.opsForValue().set(presenceKey, "online", Duration.ofMinutes(5));
    }

    // Remove session and return whether user is now offline
    public boolean markSessionOffline(String userId, String sessionId) {
        String sessionKey = "presence:user:" + userId + ":sessions";
        redisTemplate.opsForSet().remove(sessionKey, sessionId);
        Long remaining = redisTemplate.opsForSet().size(sessionKey);

        if (remaining == null || remaining == 0) {
            redisTemplate.delete(sessionKey);
            redisTemplate.delete("presence:user:" + userId);
            return true; // No active sessions left
        }
        return false;
    }
    public boolean isUserOnline(String userId) {
        return redisTemplate.hasKey("presence:user:" + userId);
    }
    // ⏰ Refresh TTL every 30 seconds for active users
//    @Scheduled(fixedRate = 30000)
//    public void refreshOnlineUsers() {
//        for (String userId : onlineUsers) {
//            redisTemplate.expire("presence:user:" + userId, Duration.ofSeconds(60));
//        }
//    }
}

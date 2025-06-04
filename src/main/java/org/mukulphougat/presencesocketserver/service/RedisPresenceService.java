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
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    public RedisPresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markUserOnline(String userId) {
        redisTemplate.opsForValue().set("presence:user:" + userId, "online", Duration.ofSeconds(60));
        onlineUsers.add(userId);
    }

    public void markUserOffline(String userId) {
        redisTemplate.delete("presence:user:" + userId);
        onlineUsers.remove(userId);
    }

    public boolean isUserOnline(String userId) {
        return redisTemplate.hasKey("presence:user:" + userId);
    }
    // ⏰ Refresh TTL every 30 seconds for active users
    @Scheduled(fixedRate = 30000)
    public void refreshOnlineUsers() {
        for (String userId : onlineUsers) {
            redisTemplate.expire("presence:user:" + userId, Duration.ofSeconds(60));
        }
    }
}

package org.mukulphougat.presencesocketserver.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisPresenceService {

    private final StringRedisTemplate redisTemplate;

    public RedisPresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markUserOnline(String userId) {
        redisTemplate.opsForValue().set("presence:user:" + userId, "online", Duration.ofSeconds(60));
    }

    public void markUserOffline(String userId) {
        redisTemplate.delete("presence:user:" + userId);
    }

    public boolean isUserOnline(String userId) {
        return redisTemplate.hasKey("presence:user:" + userId);
    }
}

package org.mukulphougat.presencesocketserver.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final String secret = "f4a1b2c3d4e5f67890abcdef1234567890abcdef1234567890abcdef12345678"; // recommended length
    private final SecretKey key;
    private final JwtParser parser;

    public JwtService() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parser().verifyWith(key).build();
    }

    public String extractUserId(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        return claims.getSubject(); // or claims.get("userId", String.class) if using custom claim
    }
}
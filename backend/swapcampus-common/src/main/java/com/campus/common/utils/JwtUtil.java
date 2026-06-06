package com.campus.common.utils;

import com.campus.common.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public Long parseUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String normalizedToken = removeBearerPrefix(token);
        if (normalizedToken.matches("\\d+")) {
            return Long.valueOf(normalizedToken);
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(normalizedToken)
                .getPayload();

        Object userId = claims.get("userId");
        if (userId == null) {
            userId = claims.getSubject();
        }

        return userId == null ? null : Long.valueOf(String.valueOf(userId));
    }

    private String removeBearerPrefix(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}

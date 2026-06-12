package com.campus.common.utils;

import com.campus.common.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtConfig jwtConfig;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(Long userId, String subjectValue) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpireSeconds() * 1000L);

        return BEARER_PREFIX + Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("account", subjectValue)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    public Long parseUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String normalizedToken = removeBearerPrefix(token);
        if (normalizedToken.matches("\\d+")) {
            return Long.valueOf(normalizedToken);
        }

        Claims claims = parseClaims(normalizedToken);
        Object userId = claims.get("userId");
        if (userId == null) {
            userId = claims.getSubject();
        }

        return userId == null ? null : Long.valueOf(String.valueOf(userId));
    }

    public Long getUserIdFromToken(String token) {
        return parseUserId(token);
    }

    public String getStudentIdFromToken(String token) {
        Claims claims = parseClaims(removeBearerPrefix(token));
        Object account = claims.get("account");
        return account == null ? null : String.valueOf(account);
    }

    public boolean validateToken(String token) {
        try {
            parseUserId(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String removeBearerPrefix(String token) {
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return token;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}

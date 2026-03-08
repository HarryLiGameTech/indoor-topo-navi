package com.e611.toponavi.web.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates platform-internal JWTs.
 * These are completely separate from GitHub OAuth tokens.
 */
@Service
public class JwtService {

    @Value("${platform.jwt.secret}")
    private String secret;

    @Value("${platform.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey signingKey() {
        // HMAC-SHA256 key derived from the configured secret
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        // Pad to at least 32 bytes
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * Issues a signed JWT for the given user.
     *
     * @param userId internal platform user UUID
     * @param login  GitHub login (for convenience, stored as subject)
     */
    public String issue(UUID userId, String login) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("login", login)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Validates a JWT and returns its claims.
     *
     * @throws JwtException if the token is invalid or expired
     */
    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the platform user UUID from a valid JWT.
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(validate(token).getSubject());
    }
}


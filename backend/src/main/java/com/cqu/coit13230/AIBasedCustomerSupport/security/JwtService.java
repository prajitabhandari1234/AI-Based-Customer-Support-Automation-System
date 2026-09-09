package com.cqu.coit13230.AIBasedCustomerSupport.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Creates and validates JWT tokens used for authentication.
 * The token stores the user identity and role so protected requests can be authorised.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationTime;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = Duration.ofMinutes(expirationMinutes).toMillis();
    }

    // Stores the user email as the subject and the role as a claim before signing the token.
    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationTime);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("role", user.getRole().name())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // Accepts a token only when its subject matches the user and the expiry time has not passed.
    public boolean isTokenValid(String token, String email) {
        try {
            Claims claims = extractClaims(token);
            return claims.getSubject().equalsIgnoreCase(email)
                    && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    // Uses the configured secret key to verify the signature before reading JWT claims.
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

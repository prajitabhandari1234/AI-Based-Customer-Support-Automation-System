package com.cqu.coit13230.AIBasedCustomerSupport.security;

import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of("userId", user.getId(), "role", user.getRole().name()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, UserDetailsLookup lookup) {
        try {
            Claims claims = claims(token);
            return claims.getSubject().equalsIgnoreCase(lookup.username())
                    && claims.getExpiration().after(new Date())
                    && lookup.enabled();
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public record UserDetailsLookup(String username, boolean enabled) {}
}
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Service responsible for generating, reading, and validating
 * JSON Web Tokens (JWT) used for user authentication.
 *
 * <p>
 * JWT tokens contain the authenticated user's email address and role.
 * Tokens are cryptographically signed to prevent unauthorised modification.
 * </p>
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationTime;

    /**
     * Constructs the JWT service using security configuration values.
     *
     * @param secret JWT signing secret
     * @param expirationTime token expiration duration in milliseconds
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationTime) {

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));

        this.expirationTime = expirationTime;
    }

    /**
     * Generates a signed JWT for an authenticated user.
     *
     * @param email authenticated user's email address
     * @param role authenticated user's role
     * @return generated JWT
     */
    public String generateToken(String email, String role) {

        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + expirationTime);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the user's email address from a JWT.
     *
     * @param token JWT to process
     * @return email address stored in the token subject
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the user's role from a JWT.
     *
     * @param token JWT to process
     * @return role stored in the token
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Determines whether a JWT is valid for the specified user.
     *
     * @param token JWT to validate
     * @param email expected user email address
     * @return {@code true} when the token is valid and belongs to the user
     */
    public boolean isTokenValid(String token, String email) {

        String tokenEmail = extractEmail(token);

        return tokenEmail.equals(email)
                && !isTokenExpired(token);
    }

    /**
     * Determines whether a JWT has expired.
     *
     * @param token JWT to inspect
     * @return {@code true} if the token has expired
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /**
     * Parses and verifies the claims contained within a JWT.
     *
     * @param token JWT to parse
     * @return verified JWT claims
     */
    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.cqu.coit13230.AIBasedCustomerSupport.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.security.JwtService;

/**
 * Unit tests JWT creation, claim extraction, subject validation, expiration
 * handling and rejection of invalid or incorrectly signed tokens.
 */
class JwtServiceUnitTest {

    private static final String SECRET_A =
            "unit-test-secret-a-that-is-more-than-sixty-four-characters-long-12345678901234567890";
    private static final String SECRET_B =
            "unit-test-secret-b-that-is-more-than-sixty-four-characters-long-12345678901234567890";

    @Test
    void generatedTokenContainsExpectedIdentityAndRoleAndValidatesSubject() {
        JwtService service = new JwtService(SECRET_A, 60);
        User user = user(42L, "Case.User@Example.COM", UserRole.AGENT);

        String token = service.generateToken(user);

        assertEquals("Case.User@Example.COM", service.extractEmail(token));
        assertEquals("AGENT", service.extractRole(token));
        assertTrue(service.isTokenValid(token, "case.user@example.com"));
        assertFalse(service.isTokenValid(token, "other@example.com"));
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService service = new JwtService(SECRET_A, 0);
        User user = user(1L, "expired@example.com", UserRole.CLIENT);
        String token = service.generateToken(user);
        Thread.sleep(5);
        assertFalse(service.isTokenValid(token, user.getEmail()));
    }

    @Test
    void tokenSignedWithDifferentSecretAndMalformedTokenAreRejected() {
        JwtService signer = new JwtService(SECRET_A, 60);
        JwtService verifier = new JwtService(SECRET_B, 60);
        User user = user(2L, "user@example.com", UserRole.CLIENT);
        String token = signer.generateToken(user);

        assertFalse(verifier.isTokenValid(token, user.getEmail()));
        assertFalse(verifier.isTokenValid("not-a-jwt", user.getEmail()));
        assertThrows(Exception.class, () -> verifier.extractEmail(token));
    }

    /**
     * Builds a minimal active user for JWT unit tests.
     *
     * @param id user identifier
     * @param email user email
     * @param role user role
     * @return populated test user
     */
    private User user(Long id, String email, UserRole role) {
        User user = new User();
        user.setUserId(id);
        user.setName("JWT User");
        user.setEmail(email);
        user.setPasswordHash("not-used");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}

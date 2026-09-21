package com.cqu.coit13230.AIBasedCustomerSupport.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;

/**
 * Tests authentication and security behaviour, including registration, login,
 * JWT protection, role authorization, resource ownership and CORS rules.
 */
class AuthenticationAndSecuritySystemTest extends AbstractSystemTest {

    @Test
    void healthEndpointIsPublic() throws Exception {
        ApiResponse response = get("/actuator/health", null);
        assertEquals(200, response.status());
        assertNotNull(response.json());
        assertEquals("UP", response.json().path("status").asText());
    }

    @Test
    void registrationCreatesActiveClientNormalizesEmailAndReturnsJwt() throws Exception {
        ApiResponse response = post("/api/auth/register", null, Map.of(
                "name", "  New Customer  ",
                "email", "New.Customer@Example.COM",
                "password", "Password123!"));

        assertEquals(201, response.status());
        assertEquals("CLIENT", response.json().path("role").asText());
        assertEquals("ACTIVE", response.json().path("status").asText());
        assertFalse(response.json().path("token").asText().isBlank());
        assertEquals("new.customer@example.com", response.json().path("email").asText());
        assertEquals("New Customer", response.json().path("name").asText());
        assertFalse(response.body().contains("passwordHash"));
    }

    @Test
    void duplicateRegistrationIsCaseInsensitiveConflict() throws Exception {
        post("/api/auth/register", null, Map.of(
                "name", "One", "email", "duplicate@example.com", "password", "Password123!"));

        ApiResponse response = post("/api/auth/register", null, Map.of(
                "name", "Two", "email", "DUPLICATE@EXAMPLE.COM", "password", "Password123!"));

        assertEquals(409, response.status());
        assertEquals(409, response.json().path("status").asInt());
    }

    @Test
    void registrationValidationRejectsBlankInvalidShortAndOversizedFields() throws Exception {
        ApiResponse blank = post("/api/auth/register", null, Map.of(
                "name", " ", "email", "bad", "password", "short"));
        assertEquals(400, blank.status());
        assertTrue(blank.json().has("validationErrors"));

        ApiResponse longName = post("/api/auth/register", null, Map.of(
                "name", "x".repeat(121), "email", "valid@example.com", "password", "Password123!"));
        assertEquals(400, longName.status());

        ApiResponse longPassword = post("/api/auth/register", null, Map.of(
                "name", "Valid", "email", "valid2@example.com", "password", "x".repeat(101)));
        assertEquals(400, longPassword.status());
    }

    @Test
    void loginAcceptsEmailCaseAndRejectsWrongPassword() throws Exception {
        ApiResponse ok = post("/api/auth/login", null, Map.of(
                "email", "CLIENT1@TEST.LOCAL", "password", "Client123!"));
        assertEquals(200, ok.status());
        assertFalse(ok.json().path("token").asText().isBlank());

        ApiResponse bad = post("/api/auth/login", null, Map.of(
                "email", "client1@test.local", "password", "wrong-password"));
        assertEquals(401, bad.status());
        assertEquals("Invalid email or password", bad.json().path("message").asText());
    }

    @Test
    void inactiveAccountCannotLoginOrUsePreviouslyIssuedToken() throws Exception {
        ApiResponse inactiveLogin = post("/api/auth/login", null, Map.of(
                "email", "inactive@test.local", "password", "Client123!"));
        assertEquals(403, inactiveLogin.status());

        String issuedToken = token(clientUser);
        clientUser.setStatus(com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus.INACTIVE);
        userRepository.save(clientUser);

        ApiResponse response = get("/api/auth/me", issuedToken);
        assertTrue(response.status() == 401 || response.status() == 403);
    }

    @Test
    void protectedEndpointRejectsNoTokenMalformedTokenAndUnknownBearerScheme() throws Exception {
        ApiResponse missing = get("/api/auth/me", null);
        assertTrue(missing.status() == 401 || missing.status() == 403);

        ApiResponse malformed = get("/api/auth/me", "not-a-jwt");
        assertTrue(malformed.status() == 401 || malformed.status() == 403);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/me"))
                .header("Authorization", "Basic abc")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403);
    }

    @Test
    void roleAuthorizationMatrixProtectsAdminAgentAndCustomerRoutes() throws Exception {
        assertEquals(403, get("/api/admin/users", token(clientUser)).status());
        assertEquals(403, get("/api/agent/tickets", token(clientUser)).status());
        assertEquals(403, get("/api/customer/tickets", token(agent)).status());
        assertEquals(403, post("/api/chat/messages", token(agent), Map.of("message", "hello")).status());
        assertEquals(403, get("/api/analytics/summary", token(agent)).status());
        assertEquals(200, get("/api/admin/users", token(admin)).status());
        assertEquals(200, get("/api/agent/tickets", token(admin)).status());
    }

    @Test
    void knowledgeBaseReadIsAuthenticatedButWriteRequiresStaff() throws Exception {
        assertTrue(get("/api/knowledge-base", null).status() == 401
                || get("/api/knowledge-base", null).status() == 403);
        assertEquals(200, get("/api/knowledge-base", token(clientUser)).status());
        assertEquals(403, post("/api/knowledge-base", token(clientUser), Map.of(
                "questionPattern", "reset password",
                "answerTemplate", "Reset it",
                "category", "ACCOUNT")).status());
        assertEquals(201, post("/api/knowledge-base", token(agent), Map.of(
                "questionPattern", "reset password",
                "answerTemplate", "Reset it",
                "category", "ACCOUNT")).status());
    }

    @Test
    void ticketIdorChecksPreventCustomerAndAssignedAgentFromReadingOtherTickets() throws Exception {
        Ticket client2Ticket = createTicket(client2, agent2, com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus.ESCALATED);
        assertEquals(403, get("/api/tickets/" + client2Ticket.getTicketId(), token(clientUser)).status());
        assertEquals(403, get("/api/tickets/" + client2Ticket.getTicketId(), token(agent)).status());
        assertEquals(200, get("/api/tickets/" + client2Ticket.getTicketId(), token(agent2)).status());
        assertEquals(200, get("/api/tickets/" + client2Ticket.getTicketId(), token(admin)).status());
    }

    @Test
    void corsPreflightAllowsConfiguredOriginAndRejectsUnconfiguredOrigin() throws Exception {
        HttpRequest allowedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> allowed = client.send(allowedRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(allowed.statusCode() >= 200 && allowed.statusCode() < 300);
        assertEquals("http://localhost:5173", allowed.headers().firstValue("access-control-allow-origin").orElse(null));

        HttpRequest blockedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Origin", "https://evil.example")
                .header("Access-Control-Request-Method", "POST")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> blocked = client.send(blockedRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(blocked.statusCode() >= 400 || blocked.headers().firstValue("access-control-allow-origin").isEmpty());
    }
}

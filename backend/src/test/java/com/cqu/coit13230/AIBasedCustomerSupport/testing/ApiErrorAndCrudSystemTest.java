package com.cqu.coit13230.AIBasedCustomerSupport.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.model.SenderType;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

/**
 * Tests API error handling and CRUD operations for the main application
 * resources, including validation, authorization and missing-resource cases.
 */
class ApiErrorAndCrudSystemTest extends AbstractSystemTest {

    @Test
    void malformedJsonReturnsStructuredBadRequest() throws Exception {
        ApiResponse response = requestRaw(
                "POST",
                "/api/auth/login",
                null,
                "{\"email\":\"client1@test.local\",\"password\":");
        assertEquals(400, response.status());
        assertEquals(400, response.json().path("status").asInt());
        assertEquals("Invalid request body", response.json().path("message").asText());
    }

    @Test
    void invalidEnumInputReturnsBadRequestNotServerError() throws Exception {
        ApiResponse response = requestRaw(
                "POST",
                "/api/admin/users",
                token(admin),
                "{\"name\":\"X\",\"email\":\"x@example.com\",\"password\":\"Password123!\",\"role\":\"ROOT\",\"status\":\"ACTIVE\"}");
        assertEquals(400, response.status());
    }

    @Test
    void missingResourcesReturn404AcrossMajorEntities() throws Exception {
        assertEquals(404, get("/api/admin/users/999999", token(admin)).status());
        assertEquals(404, get("/api/tickets/999999", token(admin)).status());
        assertEquals(404, get("/api/knowledge-base/999999", token(admin)).status());
        assertEquals(404, get("/api/conversations/999999", token(admin)).status());
        assertEquals(404, get("/api/messages/999999", token(admin)).status());
        assertEquals(404, get("/api/system-logs/999999", token(admin)).status());
    }

    @Test
    void adminConversationCrudCoversValidationCreateUpdateAndDelete() throws Exception {
        ApiResponse invalid = post("/api/conversations", token(admin), Map.of(
                "status", "ACTIVE"));
        assertEquals(400, invalid.status());

        ApiResponse created = post("/api/conversations", token(admin), Map.of(
                "customer", Map.of("userId", clientUser.getUserId()),
                "status", "ACTIVE"));
        assertEquals(200, created.status());
        long id = created.json().path("conversationId").asLong();
        assertTrue(id > 0);

        ApiResponse updated = put("/api/conversations/" + id, token(admin), Map.of(
                "customer", Map.of("userId", clientUser.getUserId()),
                "status", "COMPLETED"));
        assertEquals(200, updated.status());
        assertEquals("COMPLETED", updated.json().path("status").asText());

        assertEquals(204, delete("/api/conversations/" + id, token(admin)).status());
        assertEquals(404, get("/api/conversations/" + id, token(admin)).status());
    }

    @Test
    void adminMessageCrudAndValidationWorkWithPersistedConversation() throws Exception {
        Conversation conversation = createConversation(clientUser);
        ApiResponse invalid = post("/api/messages", token(admin), Map.of(
                "conversation", Map.of("conversationId", conversation.getConversationId()),
                "senderType", "CLIENT",
                "content", ""));
        assertEquals(400, invalid.status());

        ApiResponse created = post("/api/messages", token(admin), Map.of(
                "conversation", Map.of("conversationId", conversation.getConversationId()),
                "senderUser", Map.of("userId", clientUser.getUserId()),
                "senderType", "CUSTOMER",
                "content", "Original message"));
        assertEquals(200, created.status());
        long messageId = created.json().path("messageId").asLong();

        ApiResponse updated = put("/api/messages/" + messageId, token(admin), Map.of(
                "conversation", Map.of("conversationId", conversation.getConversationId()),
                "senderUser", Map.of("userId", clientUser.getUserId()),
                "senderType", "CLIENT",
                "content", "Updated message"));
        assertEquals(200, updated.status());
        assertEquals("Updated message", updated.json().path("content").asText());

        assertEquals(200, get("/api/messages/" + messageId, token(admin)).status());
        assertEquals(204, delete("/api/messages/" + messageId, token(admin)).status());
        assertEquals(404, get("/api/messages/" + messageId, token(admin)).status());
    }

    @Test
    void genericNotificationAdminCrudAndUserReadBoundaryAreEnforced() throws Exception {
        Ticket ticket = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        Notification notification = createNotification(clientUser, ticket, "Notice");

        assertEquals(403, get("/api/notifications/" + notification.getNotificationId(), token(clientUser)).status());
        assertEquals(200, get("/api/notifications/" + notification.getNotificationId(), token(admin)).status());
        assertEquals(200, patch("/api/notifications/" + notification.getNotificationId() + "/read", token(clientUser), Map.of()).status());
        assertEquals(204, delete("/api/notifications/" + notification.getNotificationId(), token(admin)).status());
    }


    @Test
    void adminTicketCrudValidatesScoreBoundsAndSupportsUpdateDelete() throws Exception {
        Conversation conversation = createConversation(clientUser);
        ApiResponse created = post("/api/tickets", token(admin), Map.of(
                "conversation", Map.of("conversationId", conversation.getConversationId()),
                "customer", Map.of("userId", clientUser.getUserId()),
                "title", "Admin-created ticket",
                "category", "TECHNICAL",
                "priority", "MEDIUM",
                "status", "OPEN",
                "sentiment", "NEUTRAL",
                "sentimentScore", 0.0,
                "aiConfidenceScore", 0.8));
        assertEquals(201, created.status());
        long ticketId = created.json().path("ticketId").asLong();
        assertTrue(ticketId > 0);
        assertEquals(200, get("/api/tickets", token(admin)).status());

        ApiResponse invalidScore = put("/api/tickets/" + ticketId, token(admin), Map.of(
                "conversation", Map.of("conversationId", conversation.getConversationId()),
                "customer", Map.of("userId", clientUser.getUserId()),
                "title", "Invalid score",
                "category", "TECHNICAL",
                "priority", "HIGH",
                "status", "OPEN",
                "sentimentScore", 1.5,
                "aiConfidenceScore", 0.8));
        assertEquals(400, invalidScore.status());

        ApiResponse updated = put("/api/tickets/" + ticketId, token(admin), Map.of(
                "conversation", Map.of("conversationId", conversation.getConversationId()),
                "customer", Map.of("userId", clientUser.getUserId()),
                "title", "Admin-updated ticket",
                "category", "ACCOUNT",
                "priority", "HIGH",
                "status", "ON_HOLD",
                "sentiment", "NEUTRAL",
                "sentimentScore", 0.0,
                "aiConfidenceScore", 1.0));
        assertEquals(200, updated.status());
        assertEquals("ON_HOLD", updated.json().path("status").asText());
        assertEquals(204, delete("/api/tickets/" + ticketId, token(admin)).status());
        assertEquals(404, get("/api/tickets/" + ticketId, token(admin)).status());
    }

    @Test
    void adminNotificationCreateUpdateAndDeleteCrudWorks() throws Exception {
        Ticket ticket = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        ApiResponse created = post("/api/notifications", token(admin), Map.of(
                "user", Map.of("userId", clientUser.getUserId()),
                "ticket", Map.of("ticketId", ticket.getTicketId()),
                "message", "Admin-created notification",
                "isRead", false));
        assertEquals(200, created.status());
        long notificationId = created.json().path("notificationId").asLong();

        ApiResponse updated = put("/api/notifications/" + notificationId, token(admin), Map.of(
                "user", Map.of("userId", clientUser.getUserId()),
                "ticket", Map.of("ticketId", ticket.getTicketId()),
                "message", "Updated notification",
                "isRead", true));
        assertEquals(200, updated.status());
        assertEquals("Updated notification", updated.json().path("message").asText());
        assertTrue(updated.json().path("isRead").asBoolean());

        assertEquals(204, delete("/api/notifications/" + notificationId, token(admin)).status());
    }

    @Test
    void adminUserDeleteAndGeneralUserCrudAreProtectedAndConsistent() throws Exception {
        UserHolder created = createUserViaGeneralAdminRoute();
        assertTrue(created.id > 0);
        assertEquals(403, delete("/api/users/" + created.id, token(agent)).status());
        assertEquals(204, delete("/api/users/" + created.id, token(admin)).status());
        assertEquals(404, get("/api/users/" + created.id, token(admin)).status());
    }

    /**
     * Creates a user through the general admin route for CRUD tests.
     *
     * @return holder containing the created user identifier
     * @throws Exception if the API request fails
     */
    private UserHolder createUserViaGeneralAdminRoute() throws Exception {
        ApiResponse response = post("/api/users", token(admin), Map.of(
                "name", "General User",
                "email", "general@example.com",
                "passwordHash", "already-hashed-for-api-crud-test",
                "role", "CLIENT",
                "status", "ACTIVE"));
        assertEquals(200, response.status());
        assertFalse(response.body().contains("passwordHash"));
        return new UserHolder(response.json().path("userId").asLong());
    }

    @Test
    void ticketMessageAcceptsMessageOrContentAliasesButRejectsBothBlank() throws Exception {
        Ticket ticket = createTicket(clientUser, null, TicketStatus.OPEN);
        assertEquals(200, post("/api/tickets/" + ticket.getTicketId() + "/messages", token(clientUser), Map.of(
                "message", "Using message field")).status());
        assertEquals(200, post("/api/tickets/" + ticket.getTicketId() + "/messages", token(clientUser), Map.of(
                "content", "Using content field")).status());
        assertEquals(400, post("/api/tickets/" + ticket.getTicketId() + "/messages", token(clientUser), Map.of(
                "message", " ", "content", " ")).status());
    }

    @Test
    void sqlAndHtmlLikeStringsAreHandledAsDataRatherThanChangingAuthorization() throws Exception {
        ApiResponse login = post("/api/auth/login", null, Map.of(
                "email", "' OR '1'='1",
                "password", "anything"));
        assertEquals(400, login.status()); // invalid email is rejected before authentication.

        ApiResponse register = post("/api/auth/register", null, Map.of(
                "name", "<script>alert(1)</script>",
                "email", "safe-html@example.com",
                "password", "Password123!"));
        assertEquals(201, register.status());
        assertEquals("<script>alert(1)</script>", register.json().path("name").asText());
        assertFalse(register.json().path("token").asText().isBlank());
    }

    /**
     * Keeps the identifier returned when a test user is created.
     *
     * @param id created user identifier
     */
    private record UserHolder(long id) {}
}

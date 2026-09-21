package com.cqu.coit13230.AIBasedCustomerSupport.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

/**
 * Tests agent and administrator workflows across ticket assignment, replies,
 * status changes, notifications, user management, knowledge base, analytics
 * and system logging endpoints.
 */
class AgentAdminWorkflowSystemTest extends AbstractSystemTest {

    @Test
    void agentCanAssignUnassignedTicketToSelfAndCustomerIsNotified() throws Exception {
        Ticket ticket = createTicket(clientUser, null, TicketStatus.ESCALATED);

        ApiResponse response = put("/api/agent/tickets/" + ticket.getTicketId() + "/assign", token(agent), Map.of());

        assertEquals(200, response.status());
        Ticket updated = ticketRepository.findById(ticket.getTicketId()).orElseThrow();
        assertEquals(agent.getUserId(), updated.getAssignedAgent().getUserId());
        assertEquals(TicketStatus.IN_PROGRESS, updated.getStatus());
        assertNotNull(updated.getEscalatedAt());
        assertTrue(notificationRepository.findByUserUserIdOrderByCreatedAtDesc(clientUser.getUserId()).size() >= 1);
    }

    @Test
    void agentCannotAssignTicketToDifferentAgentButAdminCan() throws Exception {
        Ticket ticket = createTicket(clientUser, null, TicketStatus.ESCALATED);

        ApiResponse forbidden = patch(
                "/api/agent/tickets/" + ticket.getTicketId() + "/assign/" + agent2.getUserId(),
                token(agent),
                Map.of());
        assertEquals(403, forbidden.status());

        ApiResponse adminAssigned = patch(
                "/api/agent/tickets/" + ticket.getTicketId() + "/assign/" + agent2.getUserId(),
                token(admin),
                Map.of());
        assertEquals(200, adminAssigned.status());
        assertEquals(agent2.getUserId().longValue(), adminAssigned.json().path("assignedAgent").path("userId").asLong());
    }

    @Test
    void assigningInactiveOrNonAgentUserIsBadRequest() throws Exception {
        User inactiveAgent = saveUser("Inactive Agent", "inactive.agent@test.local", "Agent123!", UserRole.AGENT, UserStatus.INACTIVE);
        Ticket ticket = createTicket(clientUser, null, TicketStatus.ESCALATED);

        assertEquals(400, patch(
                "/api/agent/tickets/" + ticket.getTicketId() + "/assign/" + inactiveAgent.getUserId(),
                token(admin), Map.of()).status());
        assertEquals(400, patch(
                "/api/agent/tickets/" + ticket.getTicketId() + "/assign/" + clientUser.getUserId(),
                token(admin), Map.of()).status());
    }

    @Test
    void agentReplyRequiresAssignmentAndMovesTicketToInProgress() throws Exception {
        Ticket assigned = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        Ticket other = createTicket(clientUser, agent2, TicketStatus.ESCALATED);

        ApiResponse ok = post("/api/agent/tickets/" + assigned.getTicketId() + "/messages", token(agent), Map.of(
                "content", "I am looking into this now."));
        assertEquals(200, ok.status());
        Ticket updated = ticketRepository.findById(assigned.getTicketId()).orElseThrow();
        assertEquals(TicketStatus.IN_PROGRESS, updated.getStatus());
        assertNotNull(updated.getFirstResponseAt());
        assertTrue(notificationRepository.findByUserUserIdOrderByCreatedAtDesc(clientUser.getUserId()).size() >= 1);

        ApiResponse forbidden = post("/api/agent/tickets/" + other.getTicketId() + "/messages", token(agent), Map.of(
                "content", "I should not be able to reply."));
        assertEquals(403, forbidden.status());
    }

    @Test
    void agentMessageValidationRejectsBlankAndOverTenThousandCharacters() throws Exception {
        Ticket ticket = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        assertEquals(400, post("/api/agent/tickets/" + ticket.getTicketId() + "/messages", token(agent), Map.of(
                "content", "   ")).status());
        assertEquals(400, post("/api/agent/tickets/" + ticket.getTicketId() + "/messages", token(agent), Map.of(
                "content", "x".repeat(10001))).status());
    }

    @Test
    void statusResolutionAndClosureSetTimestampsAndNotifyCustomer() throws Exception {
        Ticket ticket = createTicket(clientUser, agent, TicketStatus.IN_PROGRESS);

        ApiResponse resolved = patch("/api/agent/tickets/" + ticket.getTicketId() + "/status", token(agent), Map.of(
                "status", "RESOLVED",
                "resolutionNotes", " Fixed successfully. "));
        assertEquals(200, resolved.status());
        Ticket afterResolved = ticketRepository.findById(ticket.getTicketId()).orElseThrow();
        assertEquals(TicketStatus.RESOLVED, afterResolved.getStatus());
        assertNotNull(afterResolved.getResolvedAt());
        assertEquals("Fixed successfully.", afterResolved.getResolutionNotes());

        ApiResponse closed = patch("/api/agent/tickets/" + ticket.getTicketId() + "/status", token(agent), Map.of(
                "status", "CLOSED"));
        assertEquals(200, closed.status());
        Ticket afterClosed = ticketRepository.findById(ticket.getTicketId()).orElseThrow();
        assertEquals(TicketStatus.CLOSED, afterClosed.getStatus());
        assertNotNull(afterClosed.getClosedAt());
        assertNotNull(afterClosed.getResolvedAt());
        assertTrue(notificationRepository.findByUserUserIdOrderByCreatedAtDesc(clientUser.getUserId()).size() >= 2);
    }

    @Test
    void agentCannotUpdateTicketAssignedToAnotherAgent() throws Exception {
        Ticket ticket = createTicket(clientUser, agent2, TicketStatus.IN_PROGRESS);
        ApiResponse response = put("/api/agent/tickets/" + ticket.getTicketId(), token(agent), Map.of(
                "status", "RESOLVED",
                "resolutionNotes", "No access"));
        assertEquals(403, response.status());
    }

    @Test
    void statusPayloadRejectsMissingAndInvalidEnum() throws Exception {
        Ticket ticket = createTicket(clientUser, agent, TicketStatus.IN_PROGRESS);
        assertEquals(400, patch("/api/agent/tickets/" + ticket.getTicketId() + "/status", token(agent), Map.of()).status());
        assertEquals(400, requestRaw(
                "PATCH",
                "/api/agent/tickets/" + ticket.getTicketId() + "/status",
                token(agent),
                "{\"status\":\"NOT_A_STATUS\"}").status());
    }

    @Test
    void genericTicketEscalationIsAdminOnlyAndClosedTicketCannotBeEscalated() throws Exception {
        Ticket open = createTicket(clientUser, null, TicketStatus.OPEN);
        assertEquals(403, put("/api/tickets/" + open.getTicketId() + "/escalate", token(clientUser), Map.of()).status());

        ApiResponse adminEscalated = put("/api/tickets/" + open.getTicketId() + "/escalate", token(admin), Map.of());
        assertEquals(200, adminEscalated.status());
        assertEquals("ESCALATED", adminEscalated.json().path("status").asText());

        Ticket closed = createTicket(clientUser, null, TicketStatus.CLOSED);
        assertEquals(403, put("/api/tickets/" + closed.getTicketId() + "/escalate", token(admin), Map.of()).status());
    }

    @Test
    void agentTicketListOnlyShowsEscalatedUnassignedOrSelfAssignedTickets() throws Exception {
        Ticket mine = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        Ticket unassigned = createTicket(client2, null, TicketStatus.ESCALATED);
        createTicket(client2, agent2, TicketStatus.ESCALATED);
        createTicket(clientUser, agent, TicketStatus.IN_PROGRESS);

        ApiResponse response = get("/api/agent/tickets", token(agent));
        assertEquals(200, response.status());
        ApiResponse escalated = get("/api/agent/tickets/escalated", token(agent));
        assertEquals(200, escalated.status());
        String body = response.body();
        assertTrue(body.contains("\"ticketId\":" + mine.getTicketId()));
        assertTrue(body.contains("\"ticketId\":" + unassigned.getTicketId()));
        assertFalse(body.contains("\"assignedAgentId\":" + agent2.getUserId()));
    }

    @Test
    void notificationReadOperationEnforcesOwnership() throws Exception {
        Ticket ticket = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        Notification clientNotification = createNotification(clientUser, ticket, "Customer notification");
        Notification agentNotification = createNotification(agent, ticket, "Agent notification");

        ApiResponse clientList = get("/api/customer/notifications", token(clientUser));
        assertEquals(200, clientList.status());
        assertEquals(1, clientList.json().size());
        ApiResponse agentList = get("/api/agent/notifications", token(agent));
        assertEquals(200, agentList.status());
        assertEquals(1, agentList.json().size());

        ApiResponse clientRead = put(
                "/api/customer/notifications/" + clientNotification.getNotificationId() + "/read",
                token(clientUser), Map.of());
        assertEquals(200, clientRead.status());
        assertTrue(clientRead.json().path("isRead").asBoolean());

        assertEquals(403, put(
                "/api/customer/notifications/" + agentNotification.getNotificationId() + "/read",
                token(clientUser), Map.of()).status());

        assertEquals(200, put(
                "/api/agent/notifications/" + agentNotification.getNotificationId() + "/read",
                token(agent), Map.of()).status());
    }

    @Test
    void genericNotificationListShowsOnlyCurrentUsersItemsExceptForAdmin() throws Exception {
        Ticket ticket = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        createNotification(clientUser, ticket, "For client");
        createNotification(agent, ticket, "For agent");

        ApiResponse clientList = get("/api/notifications", token(clientUser));
        assertEquals(200, clientList.status());
        assertEquals(1, clientList.json().size());
        assertEquals("For client", clientList.json().get(0).path("message").asText());

        ApiResponse adminList = get("/api/notifications", token(admin));
        assertEquals(200, adminList.status());
        assertEquals(2, adminList.json().size());
    }

    @Test
    void adminCanCreateUpdatePatchAndListUsersWithValidationAndDuplicateProtection() throws Exception {
        ApiResponse created = post("/api/admin/users", token(admin), Map.of(
                "name", "New Agent",
                "email", "new.agent@example.com",
                "password", "Password123!",
                "role", "SUPPORT_AGENT",
                "status", "ACTIVE"));
        assertEquals(201, created.status());
        long userId = created.json().path("userId").asLong();
        assertFalse(created.body().contains("passwordHash"));

        ApiResponse updated = put("/api/admin/users/" + userId, token(admin), Map.of(
                "role", "AGENT",
                "status", "INACTIVE"));
        assertEquals(200, updated.status());
        assertEquals("INACTIVE", updated.json().path("status").asText());

        ApiResponse patched = patch("/api/admin/users/" + userId, token(admin), Map.of(
                "name", "Renamed Agent",
                "status", "ACTIVE"));
        assertEquals(200, patched.status());
        assertEquals("Renamed Agent", patched.json().path("name").asText());

        ApiResponse duplicate = post("/api/admin/users", token(admin), Map.of(
                "name", "Dup",
                "email", "NEW.AGENT@example.com",
                "password", "Password123!",
                "role", "AGENT",
                "status", "ACTIVE"));
        assertEquals(409, duplicate.status());

        ApiResponse invalid = post("/api/admin/users", token(admin), Map.of(
                "name", "",
                "email", "bad",
                "password", "short",
                "role", "AGENT",
                "status", "ACTIVE"));
        assertEquals(400, invalid.status());
    }

    @Test
    void activeAgentsEndpointExcludesInactiveAndNonAgentUsers() throws Exception {
        saveUser("Inactive A", "inactiveA@example.com", "Password123!", UserRole.AGENT, UserStatus.INACTIVE);
        ApiResponse response = get("/api/admin/users/agents", token(admin));
        assertEquals(200, response.status());
        String body = response.body();
        assertTrue(body.contains("agent1@test.local"));
        assertTrue(body.contains("agent2@test.local"));
        assertFalse(body.contains("inactiveA@example.com"));
        assertFalse(body.contains("client1@test.local"));
    }

    @Test
    void knowledgeBaseCrudNormalizesCategoryDefaultsActiveAndHandlesNotFound() throws Exception {
        ApiResponse created = post("/api/knowledge-base", token(admin), Map.of(
                "questionPattern", "  delivery status  ",
                "answerTemplate", "  Check tracking.  ",
                "category", " order_status "));
        assertEquals(201, created.status());
        long id = created.json().path("kbId").asLong();
        assertEquals("ORDER_STATUS", created.json().path("category").asText());
        assertTrue(created.json().path("active").asBoolean());

        ApiResponse updated = put("/api/knowledge-base/" + id, token(agent), Map.of(
                "questionPattern", "delivery tracking",
                "answerTemplate", "Use your tracking number.",
                "category", "ORDER_STATUS",
                "active", false));
        assertEquals(200, updated.status());
        assertFalse(updated.json().path("active").asBoolean());

        assertEquals(204, delete("/api/knowledge-base/" + id, token(admin)).status());
        assertEquals(404, get("/api/knowledge-base/" + id, token(admin)).status());
    }

    @Test
    void analyticsSummaryFiltersWeeklyAndMonthlyReportsReturnConsistentCounts() throws Exception {
        Ticket t1 = createTicket(clientUser, null, TicketStatus.OPEN);
        t1.setSentimentScore(-0.5);
        ticketRepository.save(t1);
        Ticket t2 = createTicket(clientUser, agent, TicketStatus.ESCALATED);
        t2.setSentimentScore(-0.8);
        ticketRepository.save(t2);
        createTicket(client2, null, TicketStatus.RESOLVED_BY_AI);

        ApiResponse summary = get("/api/analytics/summary", token(admin));
        assertEquals(200, summary.status());
        ApiResponse adminTicketSummary = get("/api/admin/analytics/tickets", token(admin));
        assertEquals(200, adminTicketSummary.status());
        assertEquals(3, summary.json().path("totalTickets").asInt());
        assertEquals(1, summary.json().path("resolvedTickets").asInt());
        assertEquals(1, summary.json().path("escalatedTickets").asInt());

        ApiResponse filtered = get(
                "/api/admin/analytics/tickets/filter?status=ESCALATED&minSentiment=-1&maxSentiment=-0.4",
                token(admin));
        assertEquals(200, filtered.status());
        assertEquals(1, filtered.json().path("totalTickets").asInt());

        String today = LocalDate.now().toString();
        ApiResponse weekly = get("/api/admin/analytics/reports/weekly?date=" + today, token(admin));
        assertEquals(200, weekly.status());
        assertEquals("WEEKLY", weekly.json().path("reportType").asText());

        ApiResponse monthly = get("/api/admin/analytics/reports/monthly?date=" + today, token(admin));
        assertEquals(200, monthly.status());
        assertEquals("MONTHLY", monthly.json().path("reportType").asText());
    }

    @Test
    void systemLogsAreAdminOnlyAndRecordImportantActions() throws Exception {
        post("/api/auth/login", null, Map.of("email", "client1@test.local", "password", "Client123!"));
        assertTrue(systemLogRepository.count() >= 1);
        assertEquals(403, get("/api/system-logs", token(agent)).status());
        ApiResponse adminLogs = get("/api/system-logs", token(admin));
        assertEquals(200, adminLogs.status());
        assertTrue(adminLogs.json().size() >= 1);
    }
}

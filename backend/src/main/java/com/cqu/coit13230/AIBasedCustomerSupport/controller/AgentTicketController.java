package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentMessageRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentTicketUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketStatusRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;

import jakarta.validation.Valid;

/**
 * Handles ticket endpoints used by support agents and administrators.
 *
 * <p>
 * This controller provides REST API operations that allow support staff
 * to view tickets, retrieve escalated tickets, assign tickets, update
 * assigned tickets, send responses, update ticket status, and assign
 * tickets to specific support agents.
 * </p>
 *
 * <p>
 * Authentication information is used where required to identify the
 * currently logged-in support staff member performing the operation.
 * </p>
 */
@RestController
@RequestMapping("/api/agent/tickets")
public class AgentTicketController {

    private final TicketService ticketService;

    /**
     * Creates the agent ticket controller with the required ticket service.
     *
     * @param ticketService service responsible for ticket management operations
     */
    public AgentTicketController(TicketService ticketService) {

        this.ticketService = ticketService;

    }

    /**
     * Retrieves the tickets available to support staff.
     *
     * <p>
     * The returned ticket information is represented using ticket summary
     * responses suitable for support staff ticket views.
     * </p>
     *
     * @return a response containing a list of ticket summaries
     */
    @GetMapping
    public ResponseEntity<List<TicketSummaryResponse>> getStaffTickets() {

        return ResponseEntity.ok(ticketService.getStaffTickets());

    }

    /**
     * Retrieves all tickets that are currently in the escalated state.
     *
     * <p>
     * Escalated tickets can be reviewed by support staff so that requests
     * requiring human assistance can be handled appropriately.
     * </p>
     *
     * @return a response containing a list of escalated tickets
     */
    @GetMapping("/escalated")
    public ResponseEntity<List<Ticket>> getEscalatedTickets() {

        return ResponseEntity.ok(ticketService.getEscalatedTickets());

    }

    /**
     * Assigns a ticket to the currently authenticated support agent.
     *
     * <p>
     * The authenticated user's identity is passed to the ticket service
     * so that the ticket can be assigned to the logged-in agent.
     * </p>
     *
     * @param ticketId       unique identifier of the ticket to assign
     * @param authentication authentication information for the currently
     *                       logged-in support agent
     * @return a response containing the assigned ticket
     */
    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.assignTicketToAgent(ticketId, authentication.getName()));

    }

    /**
     * Updates a ticket assigned to the currently authenticated support agent.
     *
     * <p>
     * The update request is validated before being passed to the ticket
     * service. The authenticated user's identity is also supplied so that
     * the service can process the update for the appropriate support agent.
     * </p>
     *
     * @param ticketId       unique identifier of the ticket to update
     * @param request        validated information containing the ticket updates
     * @param authentication authentication information for the currently
     *                       logged-in support agent
     * @return a response containing the updated ticket
     */
    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateAssignedTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AgentTicketUpdateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.updateAssignedTicket(
                        ticketId,
                        request,
                        authentication.getName()));

    }

    /**
     * Sends a support-agent response to a specific ticket.
     *
     * <p>
     * The message request is validated before being processed. The
     * authenticated user's identity is passed to the ticket service so
     * that the response is associated with the logged-in support agent.
     * </p>
     *
     * @param ticketId       unique identifier of the ticket receiving the response
     * @param request        validated request containing the agent's message
     * @param authentication authentication information for the currently
     *                       logged-in support agent
     * @return a response containing the created message
     */
    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<Message> sendAgentResponse(
            @PathVariable Long ticketId,
            @Valid @RequestBody AgentMessageRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.sendAgentResponse(
                        ticketId,
                        request,
                        authentication.getName()));

    }

    /**
     * Updates the status of a specific ticket using the current
     * support staff member.
     *
     * <p>
     * The supplied status request is validated before the ticket service
     * performs the status update.
     * </p>
     *
     * @param ticketId unique identifier of the ticket whose status will be updated
     * @param request  validated request containing the new ticket status
     * @return a response containing the updated ticket details
     */
    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketDetailsResponse> updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketStatusRequest request) {

        return ResponseEntity.ok(ticketService.updateStatusByCurrentStaff(ticketId, request));

    }

    /**
     * Assigns a specific support agent to a ticket.
     *
     * <p>
     * Both the ticket identifier and agent identifier are passed to the
     * ticket service so that the selected support agent can be assigned
     * to the specified ticket.
     * </p>
     *
     * @param ticketId unique identifier of the ticket to assign
     * @param agentId  unique identifier of the support agent to assign
     * @return a response containing the updated ticket details
     */
    @PatchMapping("/{ticketId}/assign/{agentId}")
    public ResponseEntity<TicketDetailsResponse> assignSpecificAgent(
            @PathVariable Long ticketId,
            @PathVariable Long agentId) {

        return ResponseEntity.ok(
                ticketService.assignTicketToSpecificAgent(ticketId, agentId));

    }

}
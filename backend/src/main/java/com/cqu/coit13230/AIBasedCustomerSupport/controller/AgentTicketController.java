package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentTicketUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for support-agent ticket operations.
 *
 * <p>
 * Endpoints provided by this controller are intended for users with
 * the {@code SUPPORT_AGENT} role and administrators where permitted
 * by the application's security configuration.
 * </p>
 */
@RestController
@RequestMapping("/api/agent/tickets")
public class AgentTicketController {

    private final TicketService ticketService;

    /**
     * Constructs a new {@code AgentTicketController}.
     *
     * @param ticketService service used to manage support tickets
     */
    public AgentTicketController(
            TicketService ticketService) {

        this.ticketService = ticketService;
    }

    /**
     * Retrieves all support tickets that have been escalated for
     * human assistance.
     *
     * <p>
     * Tickets are returned from the oldest escalated ticket to the
     * newest, allowing support agents to handle waiting requests in
     * a fair and predictable order.
     * </p>
     *
     * @return list of escalated support tickets
     */
    @GetMapping("/escalated")
    public ResponseEntity<List<Ticket>> getEscalatedTickets() {

        List<Ticket> tickets =
                ticketService.getEscalatedTickets();

        return ResponseEntity.ok(tickets);
    }

    /**
     * Assigns an escalated ticket to the authenticated support agent.
     *
     * @param ticketId unique identifier of the ticket
     * @param authentication authentication information obtained from JWT
     * @return updated support ticket
     */
    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        Ticket assignedTicket =
                ticketService.assignTicketToAgent(
                        ticketId,
                        authentication.getName());

        return ResponseEntity.ok(assignedTicket);
    }

    /**
     * Updates the status and resolution details of a ticket
     * assigned to the authenticated support agent.
     *
     * @param ticketId unique identifier of the support ticket
     * @param request updated status and resolution information
     * @param authentication authenticated support-agent information
     * @return updated support ticket
     */
    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateAssignedTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AgentTicketUpdateRequest request,
            Authentication authentication) {

        Ticket updatedTicket =
                ticketService.updateAssignedTicket(
                        ticketId,
                        request,
                        authentication.getName());

        return ResponseEntity.ok(updatedTicket);
    }
}
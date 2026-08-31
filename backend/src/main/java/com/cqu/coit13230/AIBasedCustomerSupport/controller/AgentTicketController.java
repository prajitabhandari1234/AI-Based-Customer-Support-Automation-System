package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;

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
}
package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link Ticket} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting customer support tickets through the {@link TicketService}.
 * </p>
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Constructs a new {@code TicketController} with the required
     * ticket service.
     *
     * @param ticketService service used to manage ticket operations
     */
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Retrieves all tickets.
     *
     * @return a list of all tickets
     */
    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    /**
     * Retrieves a ticket by identifier.
     *
     * @param ticketId the identifier of the ticket
     * @return the requested ticket
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicketById(
            @PathVariable Long ticketId) {

        return ResponseEntity.ok(
                ticketService.getTicketById(ticketId));
    }

    /**
     * Creates a new ticket.
     *
     * @param ticket the ticket to create
     * @return the created ticket
     */
    @PostMapping
    public Ticket createTicket(
            @Valid @RequestBody Ticket ticket) {

        return ticketService.saveTicket(ticket);
    }

    /**
     * Updates an existing ticket.
     *
     * @param ticketId the identifier of the ticket to update
     * @param ticket the updated ticket information
     * @return the updated ticket
     */
    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody Ticket ticket) {

        ticketService.getTicketById(ticketId);

        ticket.setTicketId(ticketId);

        return ResponseEntity.ok(
                ticketService.saveTicket(ticket));
    }

    /**
     * Deletes a ticket by identifier.
     *
     * @param ticketId the identifier of the ticket to delete
     * @return HTTP 204 if successfully deleted
     */
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long ticketId) {

        ticketService.getTicketById(ticketId);

        ticketService.deleteTicket(ticketId);

        return ResponseEntity.noContent().build();
    }
}
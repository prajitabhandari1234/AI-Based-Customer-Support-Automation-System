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
     * @return the requested ticket, or HTTP 404 if not found
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicketById(
            @PathVariable Long ticketId) {

        return ticketService.getTicketById(ticketId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new ticket.
     *
     * @param ticket the ticket to create
     * @return the created ticket
     */
    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketService.saveTicket(ticket);
    }

    /**
     * Updates an existing ticket.
     *
     * @param ticketId the identifier of the ticket to update
     * @param ticket the updated ticket information
     * @return the updated ticket, or HTTP 404 if not found
     */
    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody Ticket ticket) {

        return ticketService.getTicketById(ticketId)
                .map(existingTicket -> {
                    ticket.setTicketId(ticketId);
                    return ResponseEntity.ok(
                            ticketService.saveTicket(ticket));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a ticket by identifier.
     *
     * @param ticketId the identifier of the ticket to delete
     * @return HTTP 204 if deleted, or HTTP 404 if not found
     */
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long ticketId) {

        if (ticketService.getTicketById(ticketId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ticketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
}
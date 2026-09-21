package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;

import jakarta.validation.Valid;

/**
 * Handles ticket endpoints for logged-in customers.
 *
 * <p>
 * This controller provides REST API operations that allow authenticated
 * customers to create support tickets, view their ticket history, and
 * retrieve detailed information about individual tickets.
 * </p>
 *
 * <p>
 * Customer identity is obtained from the authenticated session so that
 * ticket operations are performed only for tickets belonging to the
 * currently logged-in customer.
 * </p>
 */
@RestController
@RequestMapping("/api/customer/tickets")
public class CustomerTicketController {

    private final TicketService ticketService;

    /**
     * Creates the customer ticket controller with the required ticket service.
     *
     * @param ticketService service responsible for customer ticket
     *                      management operations
     */
    public CustomerTicketController(TicketService ticketService) {

        this.ticketService = ticketService;

    }

    /**
     * Creates a new support ticket for the currently authenticated customer.
     *
     * <p>
     * The ticket creation request is validated before being passed to the
     * ticket service. The authenticated customer's identity is also supplied
     * so that the newly created ticket is associated with the logged-in
     * customer.
     * </p>
     *
     * <p>
     * A successful ticket creation returns an HTTP 201 Created response
     * containing the newly created ticket.
     * </p>
     *
     * @param request        validated request containing the information required
     *                       to create the support ticket
     * @param authentication authentication information for the currently
     *                       logged-in customer
     * @return a response containing the newly created ticket
     */
    @PostMapping
    public ResponseEntity<Ticket> createTicket(

            @Valid @RequestBody CreateTicketRequest request,

            Authentication authentication) {

        return ResponseEntity

                .status(HttpStatus.CREATED)

                .body(ticketService.createCustomerTicket(request, authentication.getName()));

    }

    /**
     * Retrieves the ticket history of the currently authenticated customer.
     *
     * <p>
     * The authenticated customer's identity is passed to the ticket service
     * so that only tickets associated with the logged-in customer are
     * returned.
     * </p>
     *
     * @param authentication authentication information for the currently
     *                       logged-in customer
     * @return a response containing the customer's ticket history
     */
    @GetMapping
    public ResponseEntity<List<Ticket>> getTicketHistory(Authentication authentication) {

        return ResponseEntity.ok(ticketService.getCustomerTicketHistory(authentication.getName()));

    }

    /**
     * Retrieves detailed information about a specific customer ticket.
     *
     * <p>
     * The ticket identifier and authenticated customer's identity are
     * passed to the ticket service so that the requested ticket details
     * can be retrieved for the logged-in customer.
     * </p>
     *
     * @param ticketId       unique identifier of the ticket to retrieve
     * @param authentication authentication information for the currently
     *                       logged-in customer
     * @return a response containing the detailed ticket information
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailsResponse> getTicketDetails(

            @PathVariable Long ticketId,

            Authentication authentication) {

        return ResponseEntity.ok(

                ticketService.getCustomerTicketDetails(ticketId, authentication.getName()));

    }

}
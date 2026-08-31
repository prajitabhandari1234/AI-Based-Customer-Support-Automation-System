package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for customer-specific support ticket
 * operations.
 *
 * <p>
 * Endpoints provided by this controller are intended for authenticated
 * users with the {@code CUSTOMER} role. Customer identity is obtained
 * from the authenticated JWT rather than being supplied directly in
 * the request body.
 * </p>
 */
@RestController
@RequestMapping("/api/customer/tickets")
public class CustomerTicketController {

    private final TicketService ticketService;

    /**
     * Constructs a new {@code CustomerTicketController}.
     *
     * @param ticketService service used to manage support tickets
     */
    public CustomerTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Creates a support ticket for the authenticated customer.
     *
     * <p>
     * The authenticated customer's email address is obtained from the
     * Spring Security authentication context. The service verifies that
     * the supplied conversation belongs to that customer before creating
     * the ticket.
     * </p>
     *
     * @param request information required to create the ticket
     * @param authentication authentication information obtained from the JWT
     * @return the newly created support ticket
     */
    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        Ticket createdTicket = ticketService.createCustomerTicket(
                request,
                authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTicket);
    }

    /**
     * Retrieves the support ticket history of the authenticated customer.
     *
     * <p>
     * Customer identity is obtained from the authenticated JWT rather
     * than from a request parameter. This ensures that customers can
     * retrieve only tickets associated with their own account.
     * </p>
     *
     * @param authentication authentication information obtained from the JWT
     * @return list of tickets belonging to the authenticated customer
     */
    @GetMapping
    public ResponseEntity<List<Ticket>> getTicketHistory(
            Authentication authentication) {

        List<Ticket> tickets =
                ticketService.getCustomerTicketHistory(
                        authentication.getName());

        return ResponseEntity.ok(tickets);
    }
}
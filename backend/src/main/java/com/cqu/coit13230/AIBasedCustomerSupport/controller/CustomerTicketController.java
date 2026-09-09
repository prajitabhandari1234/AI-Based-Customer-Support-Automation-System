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
 * These routes only work with tickets belonging to the logged-in customer.
 */
@RestController
@RequestMapping("/api/customer/tickets")
public class CustomerTicketController {

    private final TicketService ticketService;

    public CustomerTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketService.createCustomerTicket(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getTicketHistory(Authentication authentication) {
        return ResponseEntity.ok(ticketService.getCustomerTicketHistory(authentication.getName()));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailsResponse> getTicketDetails(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getCustomerTicketDetails(ticketId, authentication.getName()));
    }
}

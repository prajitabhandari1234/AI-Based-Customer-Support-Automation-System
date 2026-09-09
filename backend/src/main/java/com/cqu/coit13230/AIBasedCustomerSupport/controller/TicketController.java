package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketMessageRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

/**
 * Handles general ticket endpoints shared across user roles.
 * It contains the shared ticket operations that are not limited to one user role.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public TicketController(
            TicketService ticketService,
            UserService userService,
            ObjectMapper objectMapper) {

        this.ticketService = ticketService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/my")
    public List<TicketSummaryResponse> getMyTickets() {
        return ticketService.getMyTicketSummaries();
    }

    @GetMapping("/my/summary")
    public Map<String, Long> getMyTicketSummary() {
        return Map.of(
                "total", ticketService.getMyTotalCount(),
                "open", ticketService.getMyOpenCount());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailsResponse> getTicketById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getAccessibleTicketDetails(ticketId));
    }

    /*
     * Supports both manual ticket data and chat-style message requests.
     * This keeps older frontend requests working while the newer chat flow can use the same endpoint.
     */
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody JsonNode body) {
        User current = userService.currentUser();

        if (current.getRole() == UserRole.CLIENT) {
            CreateTicketRequest request = objectMapper.convertValue(body, CreateTicketRequest.class);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ticketService.createManualTicket(request));
        }

        Ticket ticket = objectMapper.convertValue(body, Ticket.class);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketService.saveTicket(ticket));
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody Ticket ticket) {

        ticketService.getTicketById(ticketId);
        ticket.setTicketId(ticketId);
        return ResponseEntity.ok(ticketService.saveTicket(ticket));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{ticketId}/escalate")
    public ResponseEntity<Ticket> escalateTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.escalateTicket(ticketId));
    }

    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<TicketDetailsResponse> addMessage(
            @PathVariable Long ticketId,
            @RequestBody TicketMessageRequest request) {

        return ResponseEntity.ok(ticketService.addManualMessage(ticketId, request));
    }
}

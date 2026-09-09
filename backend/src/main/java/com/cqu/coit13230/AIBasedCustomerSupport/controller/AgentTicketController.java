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
 * Handles ticket endpoints used by agents and admins.
 * These routes let support staff view, assign, update and reply to tickets.
 */
@RestController
@RequestMapping("/api/agent/tickets")
public class AgentTicketController {

    private final TicketService ticketService;

    public AgentTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<TicketSummaryResponse>> getStaffTickets() {
        return ResponseEntity.ok(ticketService.getStaffTickets());
    }

    @GetMapping("/escalated")
    public ResponseEntity<List<Ticket>> getEscalatedTickets() {
        return ResponseEntity.ok(ticketService.getEscalatedTickets());
    }

    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.assignTicketToAgent(ticketId, authentication.getName()));
    }

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

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketDetailsResponse> updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketStatusRequest request) {

        return ResponseEntity.ok(ticketService.updateStatusByCurrentStaff(ticketId, request));
    }

    @PatchMapping("/{ticketId}/assign/{agentId}")
    public ResponseEntity<TicketDetailsResponse> assignSpecificAgent(
            @PathVariable Long ticketId,
            @PathVariable Long agentId) {

        return ResponseEntity.ok(
                ticketService.assignTicketToSpecificAgent(ticketId, agentId));
    }
}

package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent/tickets")
public class AgentTicketController {
    private final TicketService service;

    public AgentTicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<TicketSummary> staffTickets() {
        return service.staffTickets();
    }

    @PatchMapping("/{id}/status")
    public TicketDetail updateStatus(@PathVariable Long id, @Valid @RequestBody TicketStatusRequest request) {
        return service.updateStatus(id, request);
    }

    @PatchMapping("/{id}/assign/{agentId}")
    public TicketDetail assign(@PathVariable Long id, @PathVariable Long agentId) {
        return service.assign(id, agentId);
    }
}

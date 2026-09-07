package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping("/my")
    public List<TicketSummary> myTickets() {
        return service.myTickets();
    }

    @GetMapping("/my/summary")
    public Map<String, Long> mySummary() {
        return Map.of("total", service.myTotalCount(), "open", service.myOpenCount());
    }

    @GetMapping("/{id}")
    public TicketDetail detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetail create(@Valid @RequestBody CreateTicketRequest request) {
        return service.createManual(request);
    }

    @PostMapping("/{id}/messages")
    public TicketDetail addMessage(@PathVariable Long id, @Valid @RequestBody TicketMessageRequest request) {
        return service.addManualMessage(id, request);
    }
}

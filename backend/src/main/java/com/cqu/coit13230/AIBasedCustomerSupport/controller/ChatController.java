package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.ChatRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.ChatResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;

import jakarta.validation.Valid;

/**
 * Handles customer chat requests processed by the AI service.
 * It decides whether a message starts a new chat or continues an existing ticket.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final TicketService ticketService;

    public ChatController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request) {

        // No ticket id means this is the first message; otherwise the existing chat is continued.
        ChatResponse response = request.getTicketId() == null
                ? ticketService.startChat(request.getMessage())
                : ticketService.continueChat(request.getTicketId(), request.getMessage());

        return ResponseEntity.ok(response);
    }
}

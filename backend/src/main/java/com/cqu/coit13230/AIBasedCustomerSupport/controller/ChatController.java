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
 *
 * <p>
 * This controller provides the REST API endpoint used by customers
 * to send messages through the support chat system.
 * </p>
 *
 * <p>
 * The controller determines whether the submitted message starts a
 * new chat or continues an existing ticket based on whether a ticket
 * identifier is included in the request.
 * </p>
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final TicketService ticketService;

    /**
     * Creates the chat controller with the required ticket service.
     *
     * @param ticketService service responsible for starting and continuing
     *                      customer support chat conversations
     */
    public ChatController(TicketService ticketService) {

        this.ticketService = ticketService;

    }

    /**
     * Processes a customer message submitted through the chat endpoint.
     *
     * <p>
     * If the request does not contain a ticket identifier, the message
     * is treated as the first message of a new chat and a new chat is
     * started. If a ticket identifier is provided, the message is added
     * to the existing chat associated with that ticket.
     * </p>
     *
     * @param request validated chat request containing the customer's
     *                message and an optional ticket identifier
     * @return a response containing the result of the processed chat message
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request) {

        /**
         * No ticket id means this is the first message; otherwise
         * the existing chat is continued.
         */
        ChatResponse response = request.getTicketId() == null
                ? ticketService.startChat(request.getMessage())
                : ticketService.continueChat(request.getTicketId(), request.getMessage());

        return ResponseEntity.ok(response);

    }

}
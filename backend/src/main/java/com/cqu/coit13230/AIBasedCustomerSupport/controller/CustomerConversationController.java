package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.service.ConversationService;

/**
 * Handles conversation creation for logged-in customers.
 * It creates conversations using the identity of the logged-in customer.
 */
@RestController
@RequestMapping("/api/customer/conversations")
public class CustomerConversationController {

    private final ConversationService conversationService;

    public CustomerConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(Authentication authentication) {
        Conversation conversation = conversationService.createCustomerConversation(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }
}

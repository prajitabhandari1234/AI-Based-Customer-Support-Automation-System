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
 * REST controller responsible for customer-specific conversation operations.
 *
 * <p>
 * Customers can create new support conversations using their authenticated
 * identity. The customer identity is obtained from the JWT rather than being
 * supplied by the client.
 * </p>
 *
 * <p>
 * Access to these endpoints is restricted to users with the CUSTOMER role
 * through the application's security configuration.
 * </p>
 */
@RestController
@RequestMapping("/api/customer/conversations")
public class CustomerConversationController {

    private final ConversationService conversationService;

    /**
     * Constructs the customer conversation controller.
     *
     * @param conversationService service used to manage conversations
     */
    public CustomerConversationController(
            ConversationService conversationService) {

        this.conversationService = conversationService;
    }

    /**
     * Creates a new conversation for the authenticated customer.
     *
     * @param authentication current authenticated user information
     * @return newly created customer conversation
     */
    @PostMapping
    public ResponseEntity<Conversation> createConversation(
            Authentication authentication) {

        Conversation conversation =
                conversationService.createCustomerConversation(
                        authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conversation);
    }
}
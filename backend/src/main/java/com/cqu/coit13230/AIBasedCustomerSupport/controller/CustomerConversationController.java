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
 * Handles conversation creation for authenticated customers.
 *
 * <p>
 * This controller provides the REST API endpoint used by logged-in
 * customers to create new support conversations.
 * </p>
 *
 * <p>
 * The identity of the currently authenticated customer is used when
 * creating the conversation so that the new conversation is associated
 * with the logged-in customer.
 * </p>
 */
@RestController
@RequestMapping("/api/customer/conversations")
public class CustomerConversationController {

    private final ConversationService conversationService;

    /**
     * Creates the customer conversation controller with the required
     * conversation service.
     *
     * @param conversationService service responsible for creating
     *                            customer conversations
     */
    public CustomerConversationController(ConversationService conversationService) {

        this.conversationService = conversationService;

    }

    /**
     * Creates a new conversation for the currently authenticated customer.
     *
     * <p>
     * The authenticated user's identity is obtained from the
     * {@link Authentication} object and passed to the conversation service
     * when creating the new customer conversation.
     * </p>
     *
     * <p>
     * A successful conversation creation returns an HTTP 201 Created
     * response containing the newly created conversation.
     * </p>
     *
     * @param authentication authentication information for the currently
     *                       logged-in customer
     * @return a response containing the newly created conversation
     */
    @PostMapping
    public ResponseEntity<Conversation> createConversation(Authentication authentication) {

        Conversation conversation = conversationService.createCustomerConversation(authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);

    }

}
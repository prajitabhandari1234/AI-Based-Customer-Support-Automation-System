package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.service.ConversationService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link Conversation} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting conversations through the {@link ConversationService}.
 * </p>
 *
 * <p>
 * These generic conversation endpoints are intended for administrator
 * access. Customer-specific conversation creation is handled through
 * {@code /api/customer/conversations}.
 * </p>
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Constructs a new {@code ConversationController} with the required
     * conversation service.
     *
     * @param conversationService service used to manage conversation operations
     */
    public ConversationController(
            ConversationService conversationService) {

        this.conversationService = conversationService;
    }

    /**
     * Retrieves all conversations.
     *
     * @return a list of all conversations
     */
    @GetMapping
    public List<Conversation> getAllConversations() {

        return conversationService.getAllConversations();
    }

    /**
     * Retrieves a conversation by identifier.
     *
     * @param conversationId the identifier of the conversation
     * @return the requested conversation
     * @throws ResourceNotFoundException if no conversation exists
     *         with the specified identifier
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversationById(
            @PathVariable Long conversationId) {

        return ResponseEntity.ok(
                conversationService.getConversationById(
                        conversationId));
    }

    /**
     * Creates a new conversation.
     *
     * @param conversation the conversation to create
     * @return the created conversation
     */
    @PostMapping
    public Conversation createConversation(
            @Valid @RequestBody Conversation conversation) {

        return conversationService
                .saveConversation(conversation);
    }

    /**
     * Updates an existing conversation.
     *
     * @param conversationId the identifier of the conversation to update
     * @param conversation the updated conversation information
     * @return the updated conversation
     * @throws ResourceNotFoundException if no conversation exists
     *         with the specified identifier
     */
    @PutMapping("/{conversationId}")
    public ResponseEntity<Conversation> updateConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody Conversation conversation) {

        conversationService
                .getConversationById(conversationId);

        conversation.setConversationId(
                conversationId);

        return ResponseEntity.ok(
                conversationService
                        .saveConversation(conversation));
    }

    /**
     * Deletes a conversation by identifier.
     *
     * @param conversationId the identifier of the conversation to delete
     * @return HTTP 204 when the conversation is deleted successfully
     * @throws ResourceNotFoundException if no conversation exists
     *         with the specified identifier
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long conversationId) {

        conversationService
                .getConversationById(conversationId);

        conversationService
                .deleteConversation(conversationId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
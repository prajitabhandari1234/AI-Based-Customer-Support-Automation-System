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

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.service.ConversationService;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link Conversation} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting conversations through the {@link ConversationService}.
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
    public ConversationController(ConversationService conversationService) {
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
     * @return the requested conversation, or HTTP 404 if not found
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversationById(
            @PathVariable Long conversationId) {

        return conversationService.getConversationById(conversationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new conversation.
     *
     * @param conversation the conversation to create
     * @return the created conversation
     */
    @PostMapping
    public Conversation createConversation(
            @RequestBody Conversation conversation) {

        return conversationService.saveConversation(conversation);
    }

    /**
     * Updates an existing conversation.
     *
     * @param conversationId the identifier of the conversation to update
     * @param conversation the updated conversation information
     * @return the updated conversation, or HTTP 404 if not found
     */
    @PutMapping("/{conversationId}")
    public ResponseEntity<Conversation> updateConversation(
            @PathVariable Long conversationId,
            @RequestBody Conversation conversation) {

        return conversationService.getConversationById(conversationId)
                .map(existingConversation -> {
                    conversation.setConversationId(conversationId);
                    return ResponseEntity.ok(
                            conversationService.saveConversation(conversation));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a conversation by identifier.
     *
     * @param conversationId the identifier of the conversation to delete
     * @return HTTP 204 if deleted, or HTTP 404 if not found
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long conversationId) {

        if (conversationService.getConversationById(conversationId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        conversationService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }
}
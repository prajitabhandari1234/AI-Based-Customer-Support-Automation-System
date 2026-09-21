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

import jakarta.validation.Valid;

/**
 * Provides administrator CRUD endpoints for managing conversations.
 *
 * <p>
 * This controller exposes REST API operations for retrieving, creating,
 * updating, and deleting conversation records in the customer support
 * system.
 * </p>
 *
 * <p>
 * Conversation operations are delegated to the {@link ConversationService},
 * which manages the associated business logic and data access.
 * </p>
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Creates the conversation controller with the required
     * conversation service.
     *
     * @param conversationService service responsible for managing
     *                            conversation records
     */
    public ConversationController(ConversationService conversationService) {

        this.conversationService = conversationService;

    }

    /**
     * Retrieves all conversation records available in the system.
     *
     * @return a list containing all conversations
     */
    @GetMapping
    public List<Conversation> getAllConversations() {

        return conversationService.getAllConversations();

    }

    /**
     * Retrieves a specific conversation using its unique identifier.
     *
     * @param conversationId unique identifier of the conversation to retrieve
     * @return a response containing the requested conversation
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversationById(
            @PathVariable Long conversationId) {

        return ResponseEntity.ok(
                conversationService.getConversationById(conversationId));

    }

    /**
     * Creates and stores a new conversation.
     *
     * <p>
     * The supplied conversation data is validated before being passed
     * to the conversation service for persistence.
     * </p>
     *
     * @param conversation validated conversation information to create
     * @return the newly created conversation
     */
    @PostMapping
    public Conversation createConversation(
            @Valid @RequestBody Conversation conversation) {

        return conversationService.saveConversation(conversation);

    }

    /**
     * Updates an existing conversation identified by its conversation ID.
     *
     * <p>
     * The existing conversation is first retrieved to confirm that the
     * record exists. The supplied conversation is then assigned the same
     * identifier before being saved through the conversation service.
     * </p>
     *
     * @param conversationId unique identifier of the conversation to update
     * @param conversation   validated conversation information containing
     *                       the updated values
     * @return a response containing the updated conversation
     */
    @PutMapping("/{conversationId}")
    public ResponseEntity<Conversation> updateConversation(

            @PathVariable Long conversationId,

            @Valid @RequestBody Conversation conversation) {

        /**
         * Check the record exists before reusing the same id for the
         * updated conversation.
         */
        conversationService.getConversationById(conversationId);

        conversation.setConversationId(conversationId);

        return ResponseEntity.ok(
                conversationService.saveConversation(conversation));

    }

    /**
     * Deletes a conversation identified by its unique conversation ID.
     *
     * <p>
     * After the conversation is deleted successfully, the endpoint
     * returns an HTTP 204 No Content response.
     * </p>
     *
     * @param conversationId unique identifier of the conversation to delete
     * @return an empty response indicating successful deletion
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long conversationId) {

        conversationService.deleteConversation(conversationId);

        return ResponseEntity.noContent().build();

    }

}
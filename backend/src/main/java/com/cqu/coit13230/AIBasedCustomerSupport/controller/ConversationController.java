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
 * Provides admin CRUD endpoints for conversations.
 * The controller exposes the basic conversation records through REST endpoints.
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<Conversation> getAllConversations() {
        return conversationService.getAllConversations();
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getConversationById(conversationId));
    }

    @PostMapping
    public Conversation createConversation(@Valid @RequestBody Conversation conversation) {
        return conversationService.saveConversation(conversation);
    }

    @PutMapping("/{conversationId}")
    public ResponseEntity<Conversation> updateConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody Conversation conversation) {

        // Check the record exists before reusing the same id for the updated conversation.
        conversationService.getConversationById(conversationId);
        conversation.setConversationId(conversationId);
        return ResponseEntity.ok(conversationService.saveConversation(conversation));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId) {
        conversationService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }
}

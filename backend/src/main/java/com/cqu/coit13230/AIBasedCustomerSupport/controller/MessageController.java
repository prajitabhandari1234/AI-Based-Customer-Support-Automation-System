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

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.service.MessageService;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link Message} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting conversation messages through the {@link MessageService}.
 * </p>
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    /**
     * Constructs a new {@code MessageController} with the required
     * message service.
     *
     * @param messageService service used to manage message operations
     */
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Retrieves all messages.
     *
     * @return a list of all messages
     */
    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    /**
     * Retrieves a message by identifier.
     *
     * @param messageId the identifier of the message
     * @return the requested message, or HTTP 404 if not found
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(
            @PathVariable Long messageId) {

        return messageService.getMessageById(messageId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new message.
     *
     * @param message the message to create
     * @return the created message
     */
    @PostMapping
    public Message createMessage(@RequestBody Message message) {
        return messageService.saveMessage(message);
    }

    /**
     * Updates an existing message.
     *
     * @param messageId the identifier of the message to update
     * @param message the updated message information
     * @return the updated message, or HTTP 404 if not found
     */
    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(
            @PathVariable Long messageId,
            @RequestBody Message message) {

        return messageService.getMessageById(messageId)
                .map(existingMessage -> {
                    message.setMessageId(messageId);
                    return ResponseEntity.ok(
                            messageService.saveMessage(message));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a message by identifier.
     *
     * @param messageId the identifier of the message to delete
     * @return HTTP 204 if deleted, or HTTP 404 if not found
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId) {

        if (messageService.getMessageById(messageId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
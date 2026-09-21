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

import jakarta.validation.Valid;

/**
 * Provides administrator CRUD endpoints for conversation messages.
 *
 * <p>
 * This controller exposes REST API operations for retrieving, creating,
 * updating, and deleting stored conversation messages within the customer
 * support system.
 * </p>
 *
 * <p>
 * Message operations are delegated to the {@link MessageService},
 * which manages the associated message data and persistence operations.
 * </p>
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    /**
     * Creates the message controller with the required message service.
     *
     * @param messageService service responsible for managing
     *                       conversation messages
     */
    public MessageController(MessageService messageService) {

        this.messageService = messageService;

    }

    /**
     * Retrieves all conversation messages stored in the system.
     *
     * @return a list containing all stored messages
     */
    @GetMapping
    public List<Message> getAllMessages() {

        return messageService.getAllMessages();

    }

    /**
     * Retrieves a specific message using its unique identifier.
     *
     * @param messageId unique identifier of the message to retrieve
     * @return a response containing the requested message
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long messageId) {

        return ResponseEntity.ok(messageService.getMessageById(messageId));

    }

    /**
     * Creates and stores a new conversation message.
     *
     * <p>
     * The supplied message is validated before being passed to the
     * message service for persistence.
     * </p>
     *
     * @param message validated message information to create
     * @return the newly created and stored message
     */
    @PostMapping
    public Message createMessage(@Valid @RequestBody Message message) {

        return messageService.saveMessage(message);

    }

    /**
     * Updates an existing message identified by its message ID.
     *
     * <p>
     * The existing message is first retrieved to confirm that the record
     * exists. The supplied message is then assigned the same message
     * identifier before being saved through the message service.
     * </p>
     *
     * @param messageId unique identifier of the message to update
     * @param message   validated message information containing the
     *                  updated values
     * @return a response containing the updated message
     */
    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(

            @PathVariable Long messageId,

            @Valid @RequestBody Message message) {

        messageService.getMessageById(messageId);

        message.setMessageId(messageId);

        return ResponseEntity.ok(messageService.saveMessage(message));

    }

    /**
     * Deletes a message identified by its unique message ID.
     *
     * <p>
     * After the message is successfully deleted, the endpoint returns
     * an HTTP 204 No Content response.
     * </p>
     *
     * @param messageId unique identifier of the message to delete
     * @return an empty response indicating successful deletion
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {

        messageService.deleteMessage(messageId);

        return ResponseEntity.noContent().build();

    }

}
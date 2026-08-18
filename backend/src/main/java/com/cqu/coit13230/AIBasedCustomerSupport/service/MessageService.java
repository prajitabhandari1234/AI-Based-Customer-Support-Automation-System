package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.MessageRepository;

/**
 * Service class responsible for managing {@link Message} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting conversation messages through the
 * {@link MessageRepository}.
 * </p>
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    /**
     * Constructs a new {@code MessageService} with the required
     * message repository dependency.
     *
     * @param messageRepository repository used to access message data
     */
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Creates or updates a message.
     *
     * @param message the message to be saved
     * @return the saved message
     */
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    /**
     * Retrieves all messages.
     *
     * @return a list of all messages
     */
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    /**
     * Retrieves a message by identifier.
     *
     * @param messageId the identifier of the message
     * @return an optional containing the message if found
     */
    public Optional<Message> getMessageById(Long messageId) {
        return messageRepository.findById(messageId);
    }

    /**
     * Deletes a message by identifier.
     *
     * @param messageId the identifier of the message to delete
     */
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }
}
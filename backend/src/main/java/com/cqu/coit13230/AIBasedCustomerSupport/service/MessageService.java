package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.MessageRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link Message} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting conversation messages.
 * </p>
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code MessageService} with the required
     * repository dependencies.
     *
     * @param messageRepository repository used to access message data
     * @param conversationRepository repository used to access conversation data
     * @param userRepository repository used to access user data
     */
    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository) {

        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates or updates a message.
     *
     * <p>
     * Verifies that the referenced conversation exists before saving.
     * If a sender user is provided, the user is also verified.
     * </p>
     *
     * @param message the message to be saved
     * @return the saved message
     * @throws ResourceNotFoundException if the conversation or sender user does not exist
     */
    public Message saveMessage(Message message) {

        Long conversationId = message.getConversation().getConversationId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found with ID: " + conversationId));

        message.setConversation(conversation);

        if (message.getSenderUser() != null
                && message.getSenderUser().getUserId() != null) {

            Long senderUserId = message.getSenderUser().getUserId();

            User senderUser = userRepository.findById(senderUserId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Sender user not found with ID: " + senderUserId));

            message.setSenderUser(senderUser);
        }

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
     * Retrieves a message by its unique identifier.
     *
     * @param messageId the unique identifier of the message
     * @return the message associated with the specified identifier
     * @throws ResourceNotFoundException if no message exists with the specified identifier
     */
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Message not found with ID: " + messageId));
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
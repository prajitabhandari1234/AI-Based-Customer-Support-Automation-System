package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.SenderType;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.MessageRepository;

/**
 * Provides business logic and persistence operations for messages
 * within customer-support conversations.
 *
 * <p>
 * This service manages message creation, storage, retrieval, and
 * deletion while keeping message-related database operations
 * separate from the controller layer.
 * </p>
 */
@Service
public class MessageService {

    /**
     * Repository used to perform persistence and retrieval operations
     * for conversation messages.
     */
    private final MessageRepository messageRepository;

    /**
     * Creates a message service with the repository required to
     * access message records.
     *
     * @param messageRepository repository used to access message records
     */
    public MessageService(MessageRepository messageRepository) {

        this.messageRepository = messageRepository;

    }

    /**
     * Saves the supplied message to the database.
     *
     * @param message message to persist
     * @return persisted message
     */
    public Message saveMessage(Message message) {

        return messageRepository.save(message);

    }

    /**
     * Creates and persists a new message within the specified
     * support conversation.
     *
     * <p>
     * The message is associated with its conversation, sender,
     * sender type, content, sentiment, and sentiment score.
     * When the supplied sentiment is {@code null},
     * {@link Sentiment#NEUTRAL} is used.
     * </p>
     *
     * @param conversation   conversation to which the message belongs
     * @param sender         user who sent the message
     * @param senderType     type of sender responsible for the message
     * @param content        textual content of the message
     * @param sentiment      sentiment associated with the message
     * @param sentimentScore numeric sentiment score associated with the message
     * @return newly created and persisted message
     */
    public Message createMessage(

            Conversation conversation,

            User sender,

            SenderType senderType,

            String content,

            Sentiment sentiment,

            Double sentimentScore) {

        Message message = new Message();

        message.setConversation(conversation);

        message.setSenderUser(sender);

        message.setSenderType(senderType);

        message.setContent(content);

        message.setSentiment(sentiment == null ? Sentiment.NEUTRAL : sentiment);

        message.setSentimentScore(sentimentScore);

        return messageRepository.save(message);

    }

    /**
     * Retrieves all messages stored in the database.
     *
     * @return list containing all stored messages
     */
    public List<Message> getAllMessages() {

        return messageRepository.findAll();

    }

    /**
     * Retrieves a message using its unique identifier.
     *
     * @param messageId unique identifier of the message
     * @return message matching the supplied identifier
     * @throws ResourceNotFoundException if no message exists with
     *                                   the supplied identifier
     */
    public Message getMessageById(Long messageId) {

        return messageRepository.findById(messageId)

                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

    }

    /**
     * Retrieves all messages associated with a specific conversation
     * in ascending order of creation time.
     *
     * @param conversationId unique identifier of the conversation
     * @return list of messages belonging to the specified conversation
     */
    public List<Message> getConversationMessages(Long conversationId) {

        return messageRepository.findByConversationConversationIdOrderByCreatedAtAsc(conversationId);

    }

    /**
     * Deletes a message using its unique identifier.
     *
     * <p>
     * The message is retrieved first to verify that it exists before
     * the repository deletion operation is performed.
     * </p>
     *
     * @param messageId unique identifier of the message to delete
     * @throws ResourceNotFoundException if no message exists with
     *                                   the supplied identifier
     */
    public void deleteMessage(Long messageId) {

        getMessageById(messageId);

        messageRepository.deleteById(messageId);

    }

}
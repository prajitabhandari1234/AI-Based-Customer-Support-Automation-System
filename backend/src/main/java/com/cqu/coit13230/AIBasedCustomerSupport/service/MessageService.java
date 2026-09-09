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
 * Handles messages stored inside support conversations.
 * It keeps message lookup and persistence logic in one place.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

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

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));
    }

    public List<Message> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversationConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public void deleteMessage(Long messageId) {
        getMessageById(messageId);
        messageRepository.deleteById(messageId);
    }
}

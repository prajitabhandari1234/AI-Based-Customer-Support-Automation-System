package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;

/**
 * Service class responsible for managing {@link Conversation} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting customer conversations through the
 * {@link ConversationRepository}.
 * </p>
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    /**
     * Constructs a new {@code ConversationService} with the required
     * conversation repository dependency.
     *
     * @param conversationRepository repository used to access conversation data
     */
    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    /**
     * Creates or updates a conversation.
     *
     * @param conversation the conversation to be saved
     * @return the saved conversation
     */
    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    /**
     * Retrieves all conversations.
     *
     * @return a list of all conversations
     */
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    /**
     * Retrieves a conversation by identifier.
     *
     * @param conversationId the identifier of the conversation
     * @return an optional containing the conversation if found
     */
    public Optional<Conversation> getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId);
    }

    /**
     * Deletes a conversation by identifier.
     *
     * @param conversationId the identifier of the conversation to delete
     */
    public void deleteConversation(Long conversationId) {
        conversationRepository.deleteById(conversationId);
    }
}
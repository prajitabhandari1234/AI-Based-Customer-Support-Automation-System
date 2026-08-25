package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link Conversation} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting conversations.
 * </p>
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code ConversationService} with the required
     * repository dependencies.
     *
     * @param conversationRepository repository used to access conversation data
     * @param userRepository repository used to access user data
     */
    public ConversationService(
            ConversationRepository conversationRepository,
            UserRepository userRepository) {

        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates or updates a conversation.
     *
     * <p>
     * Verifies that the customer associated with the conversation
     * exists before the conversation is saved.
     * </p>
     *
     * @param conversation the conversation to be saved
     * @return the saved conversation
     * @throws ResourceNotFoundException if the associated customer does not exist
     */
    public Conversation saveConversation(Conversation conversation) {

        Long customerId = conversation.getCustomer().getUserId();

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + customerId));

        conversation.setCustomer(customer);

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
     * Retrieves a conversation by its unique identifier.
     *
     * @param conversationId the unique identifier of the conversation
     * @return the conversation associated with the specified identifier
     * @throws ResourceNotFoundException if no conversation exists with the specified identifier
     */
    public Conversation getConversationById(Long conversationId) {

        return conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found with ID: "
                                        + conversationId));
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
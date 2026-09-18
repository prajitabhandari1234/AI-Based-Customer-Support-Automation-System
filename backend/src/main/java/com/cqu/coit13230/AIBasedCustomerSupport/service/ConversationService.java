package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.ConversationStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;

/**
 * Provides business logic and persistence operations for customer
 * conversations within the AI-based customer support system.
 *
 * <p>
 * This service manages conversation creation, retrieval, storage,
 * and deletion while keeping conversation-related database operations
 * separate from the controller layer.
 * </p>
 */
@Service
public class ConversationService {

    /**
     * Repository used to perform persistence operations for conversations.
     */
    private final ConversationRepository conversationRepository;

    /**
     * Service used to retrieve and validate the currently authenticated user.
     */
    private final UserService userService;

    /**
     * Creates a conversation service with the required repository and
     * user service dependencies.
     *
     * @param conversationRepository repository used to access conversation records
     * @param userService            service used to access authenticated user
     *                               information
     */
    public ConversationService(
            ConversationRepository conversationRepository,
            UserService userService) {

        this.conversationRepository = conversationRepository;

        this.userService = userService;

    }

    /**
     * Saves the supplied conversation to the database.
     *
     * @param conversation conversation to persist
     * @return persisted conversation
     */
    public Conversation saveConversation(Conversation conversation) {

        return conversationRepository.save(conversation);

    }

    /**
     * Creates a new conversation for the authenticated customer.
     *
     * <p>
     * The authenticated user's email address is compared with the
     * supplied customer email address. If the two addresses do not
     * match, the operation is rejected.
     * </p>
     *
     * @param customerEmail email address of the customer requesting
     *                      the conversation
     * @return newly created customer conversation
     * @throws ForbiddenOperationException if the authenticated customer's
     *                                     email does not match the supplied email
     */
    public Conversation createCustomerConversation(String customerEmail) {

        User customer = userService.currentUser();

        if (!customer.getEmail().equalsIgnoreCase(customerEmail)) {

            throw new ForbiddenOperationException("Authenticated customer does not match request");

        }

        return createForCustomer(customer);

    }

    /**
     * Creates and persists a new active conversation for the specified customer.
     *
     * <p>
     * The supplied customer is associated with a new conversation and
     * the conversation status is initially set to
     * {@link ConversationStatus#ACTIVE}.
     * </p>
     *
     * @param customer customer for whom the conversation is created
     * @return newly created and persisted conversation
     */
    public Conversation createForCustomer(User customer) {

        Conversation conversation = new Conversation();

        conversation.setCustomer(customer);

        conversation.setStatus(ConversationStatus.ACTIVE);

        return conversationRepository.save(conversation);

    }

    /**
     * Retrieves all conversations stored in the database.
     *
     * @return list containing all conversations
     */
    public List<Conversation> getAllConversations() {

        return conversationRepository.findAll();

    }

    /**
     * Retrieves a conversation using its unique identifier.
     *
     * @param conversationId unique identifier of the conversation
     * @return conversation matching the supplied identifier
     * @throws ResourceNotFoundException if no conversation exists with
     *                                   the supplied identifier
     */
    public Conversation getConversationById(Long conversationId) {

        return conversationRepository.findById(conversationId)

                .orElseThrow(() -> new ResourceNotFoundException(

                        "Conversation not found with ID: " + conversationId));

    }

    /**
     * Deletes a conversation using its unique identifier.
     *
     * <p>
     * The conversation is retrieved first to verify that it exists
     * before the repository delete operation is performed.
     * </p>
     *
     * @param conversationId unique identifier of the conversation to delete
     * @throws ResourceNotFoundException if no conversation exists with
     *                                   the supplied identifier
     */
    public void deleteConversation(Long conversationId) {

        getConversationById(conversationId);

        conversationRepository.deleteById(conversationId);

    }

}
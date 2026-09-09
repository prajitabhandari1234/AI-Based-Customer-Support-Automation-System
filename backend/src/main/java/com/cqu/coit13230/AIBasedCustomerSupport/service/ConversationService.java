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
 * Handles conversation creation and database operations.
 * It keeps the controller code small by handling conversation database operations here.
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserService userService;

    public ConversationService(
            ConversationRepository conversationRepository,
            UserService userService) {

        this.conversationRepository = conversationRepository;
        this.userService = userService;
    }

    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Conversation createCustomerConversation(String customerEmail) {
        User customer = userService.currentUser();

        if (!customer.getEmail().equalsIgnoreCase(customerEmail)) {
            throw new ForbiddenOperationException("Authenticated customer does not match request");
        }

        return createForCustomer(customer);
    }

    public Conversation createForCustomer(User customer) {
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(ConversationStatus.ACTIVE);
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found with ID: " + conversationId));
    }

    public void deleteConversation(Long conversationId) {
        getConversationById(conversationId);
        conversationRepository.deleteById(conversationId);
    }
}

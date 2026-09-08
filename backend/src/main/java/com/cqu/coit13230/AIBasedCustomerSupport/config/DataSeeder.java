package com.cqu.coit13230.AIBasedCustomerSupport.config;

import java.time.LocalDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.ConversationStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.SenderType;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.KnowledgeBaseEntryRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.MessageRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Adds demo data when database seeding is enabled.
 * Seeding only runs when enabled so normal application data is not recreated on every startup.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final KnowledgeBaseEntryRepository knowledgeBaseRepository;
    private final ConversationRepository conversationRepository;
    private final TicketRepository ticketRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            KnowledgeBaseEntryRepository knowledgeBaseRepository,
            ConversationRepository conversationRepository,
            TicketRepository ticketRepository,
            MessageRepository messageRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.conversationRepository = conversationRepository;
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * Creates the sample accounts and demo records only when they do not already exist.
     * This keeps repeated development starts from inserting the same seed data again.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User admin = ensureUser(
                "System Administrator",
                "admin@support.local",
                "Admin123!",
                UserRole.ADMIN);

        User agent = ensureUser(
                "Support Agent",
                "agent@support.local",
                "Agent123!",
                UserRole.AGENT);

        User client = ensureUser(
                "Demo Customer",
                "customer@support.local",
                "Customer123!",
                UserRole.CLIENT);

        if (knowledgeBaseRepository.count() == 0) {
            seedKnowledge(admin);
        }

        if (ticketRepository.count() == 0) {
            seedTickets(client, agent);
        }
    }

    private User ensureUser(
            String name,
            String email,
            String password,
            UserRole role) {

        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setPasswordHash(passwordEncoder.encode(password));
                    user.setRole(role);
                    user.setStatus(UserStatus.ACTIVE);
                    return userRepository.save(user);
                });
    }

    // Adds starter knowledge-base questions so the local AI fallback has some answers to match.
    private void seedKnowledge(User admin) {
        createKnowledge(
                "reset password, forgot password, password reset",
                "To reset your password, select Forgot password on the login page, enter your registered email, and follow the reset link.",
                TicketCategory.ACCOUNT,
                admin);

        createKnowledge(
                "track order, order status, delivery tracking",
                "You can track an order from the Orders section using your order number. If tracking has not updated for 48 hours, a support agent can investigate.",
                TicketCategory.ORDER_STATUS,
                admin);

        createKnowledge(
                "refund policy, request refund, money back",
                "Refund requests require the order number, purchase date, and reason for return. Eligible refunds are reviewed by a support agent.",
                TicketCategory.REFUND,
                admin);

        createKnowledge(
                "business hours, support hours, opening hours",
                "The AI assistant is available whenever the system is running. Human support agents respond during business hours, Monday to Friday, 9:00 AM to 5:00 PM.",
                TicketCategory.GENERAL_INQUIRY,
                admin);

        createKnowledge(
                "update email, change account email",
                "For account security, email changes are completed by a human agent after identity verification.",
                TicketCategory.ACCOUNT,
                admin);
    }

    private void createKnowledge(
            String questionPattern,
            String answerTemplate,
            TicketCategory category,
            User admin) {

        KnowledgeBaseEntry entry = new KnowledgeBaseEntry();
        entry.setQuestionPattern(questionPattern);
        entry.setAnswerTemplate(answerTemplate);
        entry.setCategory(category.name());
        entry.setActive(true);
        entry.setLastUpdatedBy(admin);
        knowledgeBaseRepository.save(entry);
    }

    // Adds tickets in different states so the dashboards and support views have useful demo data.
    private void seedTickets(User customer, User agent) {
        createDemoTicket(
                customer,
                null,
                "How do I reset my password?",
                TicketCategory.ACCOUNT,
                TicketPriority.MEDIUM,
                TicketStatus.RESOLVED_BY_AI,
                Sentiment.NEUTRAL,
                0.0,
                0.95,
                null,
                "How do I reset my password?",
                "Select Forgot password on the login page and follow the reset link sent to your registered email.");

        createDemoTicket(
                customer,
                agent,
                "Refund has not arrived",
                TicketCategory.REFUND,
                TicketPriority.HIGH,
                TicketStatus.ESCALATED,
                Sentiment.NEGATIVE,
                -0.66,
                0.58,
                "Strong negative customer sentiment was detected; ticket priority requires human review",
                "I am frustrated because my refund has not arrived and I need a human agent.",
                "I am sorry for the delay. I have escalated this ticket to a human support agent.");
    }

    private void createDemoTicket(
            User customer,
            User agent,
            String title,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            Sentiment sentiment,
            double sentimentScore,
            double confidence,
            String escalationReason,
            String customerMessage,
            String aiMessage) {

        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(
                status == TicketStatus.RESOLVED_BY_AI
                        ? ConversationStatus.COMPLETED
                        : ConversationStatus.ACTIVE);

        if (status == TicketStatus.RESOLVED_BY_AI) {
            conversation.setEndedAt(LocalDateTime.now());
        }

        conversation = conversationRepository.save(conversation);

        Ticket ticket = new Ticket();
        ticket.setConversation(conversation);
        ticket.setCustomer(customer);
        ticket.setAssignedAgent(agent);
        ticket.setTitle(title);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setSentiment(sentiment);
        ticket.setSentimentScore(sentimentScore);
        ticket.setAiConfidenceScore(confidence);
        ticket.setEscalationReason(escalationReason);
        ticket.setFirstResponseAt(LocalDateTime.now());

        if (status == TicketStatus.RESOLVED_BY_AI) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        if (status == TicketStatus.ESCALATED) {
            ticket.setEscalatedAt(LocalDateTime.now());
        }

        ticketRepository.save(ticket);

        saveMessage(
                conversation,
                customer,
                SenderType.CLIENT,
                customerMessage,
                sentiment,
                sentimentScore);

        saveMessage(
                conversation,
                null,
                SenderType.AI,
                aiMessage,
                Sentiment.NEUTRAL,
                0.0);
    }

    private void saveMessage(
            Conversation conversation,
            User sender,
            SenderType senderType,
            String content,
            Sentiment sentiment,
            double sentimentScore) {

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderUser(sender);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setSentiment(sentiment);
        message.setSentimentScore(sentimentScore);
        messageRepository.save(message);
    }
}

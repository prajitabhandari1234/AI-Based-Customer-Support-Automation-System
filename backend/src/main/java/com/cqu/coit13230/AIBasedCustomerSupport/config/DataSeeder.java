package com.cqu.coit13230.AIBasedCustomerSupport.config;

import com.cqu.coit13230.AIBasedCustomerSupport.ai.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.knowledge.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.knowledge.KnowledgeBaseRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.message.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.message.MessageRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.message.SenderType;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.*;
import com.cqu.coit13230.AIBasedCustomerSupport.user.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {
    private final UserRepository users;
    private final KnowledgeBaseRepository knowledge;
    private final TicketRepository tickets;
    private final MessageRepository messages;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository users, KnowledgeBaseRepository knowledge, TicketRepository tickets,
                      MessageRepository messages, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.knowledge = knowledge;
        this.tickets = tickets;
        this.messages = messages;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User admin = ensureUser("System Administrator", "admin@support.local", "Admin123!", Role.ADMIN);
        User agent = ensureUser("Support Agent", "agent@support.local", "Agent123!", Role.AGENT);
        User customer = ensureUser("Demo Customer", "customer@support.local", "Customer123!", Role.CLIENT);

        if (knowledge.count() == 0) {
            seedKnowledge(admin);
        }
        if (tickets.count() == 0) {
            seedTickets(customer, agent);
        }
    }

    private User ensureUser(String name, String email, String password, Role role) {
        return users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setStatus(UserStatus.ACTIVE);
            return users.save(user);
        });
    }

    private void seedKnowledge(User admin) {
        createKnowledge("reset password, forgot password, password reset",
                "To reset your password, select “Forgot password” on the login page, enter your registered email, and follow the reset link. Never share your password with support staff.",
                TicketCategory.ACCOUNT, admin);
        createKnowledge("track order, order status, delivery tracking",
                "You can track an order from the Orders section using your order number. If tracking has not updated for 48 hours, reply with the order number and a support agent will investigate.",
                TicketCategory.ORDER_STATUS, admin);
        createKnowledge("refund policy, request refund, money back",
                "Refund requests require the order number, purchase date, and reason for return. Eligible refunds are reviewed by a support agent and returned to the original payment method.",
                TicketCategory.REFUND, admin);
        createKnowledge("business hours, support hours, opening hours",
                "The AI assistant is available whenever the system is running. Human support agents respond during business hours, Monday to Friday, 9:00 AM–5:00 PM.",
                TicketCategory.GENERAL_INQUIRY, admin);
        createKnowledge("update email, change account email",
                "For account security, email changes are completed by a human agent after identity verification. Please create a ticket without including passwords or sensitive payment details.",
                TicketCategory.ACCOUNT, admin);
    }

    private void createKnowledge(String pattern, String answer, TicketCategory category, User admin) {
        KnowledgeBaseEntry entry = new KnowledgeBaseEntry();
        entry.setQuestionPattern(pattern);
        entry.setAnswerTemplate(answer);
        entry.setCategory(category);
        entry.setActive(true);
        entry.setLastUpdatedBy(admin);
        knowledge.save(entry);
    }

    private void seedTickets(User customer, User agent) {
        Ticket resolved = new Ticket();
        resolved.setTitle("How do I reset my password?");
        resolved.setCustomer(customer);
        resolved.setCategory(TicketCategory.ACCOUNT);
        resolved.setPriority(TicketPriority.MEDIUM);
        resolved.setStatus(TicketStatus.RESOLVED_BY_AI);
        resolved.setSentiment(Sentiment.NEUTRAL);
        resolved.setSentimentScore(0);
        resolved.setAiConfidenceScore(0.95);
        resolved.setEscalated(false);
        resolved = tickets.save(resolved);
        resolved.setFirstResponseAt(Instant.now());
        resolved.setResolvedAt(Instant.now());
        resolved = tickets.save(resolved);
        saveMessage(resolved, customer, SenderType.CLIENT, "How do I reset my password?", Sentiment.NEUTRAL, 0);
        saveMessage(resolved, null, SenderType.AI,
                "Select “Forgot password” on the login page and follow the reset link sent to your registered email.",
                Sentiment.NEUTRAL, 0);

        Ticket escalated = new Ticket();
        escalated.setTitle("Refund has not arrived");
        escalated.setCustomer(customer);
        escalated.setAssignedAgent(agent);
        escalated.setCategory(TicketCategory.REFUND);
        escalated.setPriority(TicketPriority.HIGH);
        escalated.setStatus(TicketStatus.ESCALATED);
        escalated.setSentiment(Sentiment.NEGATIVE);
        escalated.setSentimentScore(-0.66);
        escalated.setAiConfidenceScore(0.58);
        escalated.setEscalated(true);
        escalated.setEscalationReason("Strong negative customer sentiment was detected; Ticket priority requires human review");
        escalated = tickets.save(escalated);
        escalated.setFirstResponseAt(Instant.now());
        escalated = tickets.save(escalated);
        saveMessage(escalated, customer, SenderType.CLIENT,
                "I am frustrated because my refund has not arrived and I need a human agent.",
                Sentiment.NEGATIVE, -0.66);
        saveMessage(escalated, null, SenderType.AI,
                "I am sorry for the delay. I have escalated this ticket to a human support agent.",
                Sentiment.NEUTRAL, 0);
    }

    private void saveMessage(Ticket ticket, User sender, SenderType type, String content,
                             Sentiment sentiment, double score) {
        Message message = new Message();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setSenderType(type);
        message.setContent(content);
        message.setSentiment(sentiment);
        message.setSentimentScore(score);
        messages.save(message);
    }
}

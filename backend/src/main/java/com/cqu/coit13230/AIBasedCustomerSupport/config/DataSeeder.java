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
 * Seeds demonstration data for the AI-Based Customer Support Automation System.
 *
 * <p>
 * This component runs only when the {@code app.seed.enabled} configuration
 * property is set to {@code true}. It creates demonstration users,
 * knowledge-base entries, conversations, tickets, and messages that can be
 * used during development and testing.
 * </p>
 *
 * <p>
 * Existing users are reused based on their email addresses, while knowledge
 * base entries and tickets are created only when their corresponding
 * repositories are empty.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

        /**
         * Repository used to persist and retrieve demonstration users.
         */
        private final UserRepository userRepository;

        /**
         * Repository used to persist and retrieve knowledge-base entries.
         */
        private final KnowledgeBaseEntryRepository knowledgeBaseRepository;

        /**
         * Repository used to persist demonstration conversations.
         */
        private final ConversationRepository conversationRepository;

        /**
         * Repository used to persist and inspect demonstration tickets.
         */
        private final TicketRepository ticketRepository;

        /**
         * Repository used to persist demonstration conversation messages.
         */
        private final MessageRepository messageRepository;

        /**
         * Password encoder used to securely encode demonstration user passwords.
         */
        private final PasswordEncoder passwordEncoder;

        /**
         * Creates a data seeder with the repositories and password encoder required
         * to generate demonstration application data.
         *
         * @param userRepository          repository used to manage users
         * @param knowledgeBaseRepository repository used to manage knowledge-base
         *                                entries
         * @param conversationRepository  repository used to manage conversations
         * @param ticketRepository        repository used to manage tickets
         * @param messageRepository       repository used to manage messages
         * @param passwordEncoder         encoder used to securely encode demonstration
         *                                passwords
         */
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

        /**
         * Initializes demonstration users and application data when database
         * seeding is enabled.
         *
         * <p>
         * The method ensures that demonstration administrator, support-agent,
         * and customer accounts exist. Knowledge-base entries and demonstration
         * tickets are seeded only when their respective repositories contain
         * no existing records.
         * </p>
         *
         * @param args application arguments supplied when the Spring Boot
         *             application starts
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

        /**
         * Retrieves an existing demonstration user or creates the user when no
         * account exists with the supplied email address.
         *
         * <p>
         * Newly created users are assigned the supplied role, given active account
         * status, and stored with an encoded password.
         * </p>
         *
         * @param name     display name of the demonstration user
         * @param email    email address of the demonstration user
         * @param password password to encode for the demonstration account
         * @param role     role assigned to the demonstration user
         * @return existing or newly created demonstration user
         */
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

        /**
         * Creates the starter knowledge-base entries used by the demonstration
         * environment.
         *
         * <p>
         * The entries provide support information for password resets, order
         * tracking, refunds, business hours, and account email changes.
         * </p>
         *
         * @param admin administrator recorded as the user who last updated
         *              the knowledge-base entries
         */
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

        /**
         * Creates and persists an active knowledge-base entry.
         *
         * @param questionPattern phrases used to identify matching support questions
         * @param answerTemplate  response associated with the knowledge-base entry
         * @param category        ticket category associated with the entry
         * @param admin           administrator recorded as the last user to update the
         *                        entry
         */
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

        /**
         * Creates demonstration tickets representing different support outcomes.
         *
         * <p>
         * The seeded records include an AI-resolved account enquiry and an
         * escalated refund enquiry. These records provide sample data for
         * support views and analytics dashboards.
         * </p>
         *
         * @param customer demonstration customer associated with the tickets
         * @param agent    demonstration support agent assigned where required
         */
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

        /**
         * Creates a complete demonstration ticket with its associated conversation
         * and customer and AI messages.
         *
         * <p>
         * Conversation and ticket timestamps are updated according to whether
         * the supplied ticket represents an AI-resolved or escalated support
         * scenario.
         * </p>
         *
         * @param customer         customer associated with the demonstration ticket
         * @param agent            support agent assigned to the ticket, when applicable
         * @param title            title of the demonstration ticket
         * @param category         category assigned to the ticket
         * @param priority         priority assigned to the ticket
         * @param status           status assigned to the ticket
         * @param sentiment        sentiment associated with the customer message
         * @param sentimentScore   numerical sentiment score
         * @param confidence       AI confidence score
         * @param escalationReason reason for escalation, when applicable
         * @param customerMessage  demonstration customer message
         * @param aiMessage        demonstration AI response
         */
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

        /**
         * Creates and persists a demonstration conversation message.
         *
         * @param conversation   conversation containing the message
         * @param sender         user who sent the message, or {@code null} for an
         *                       AI-generated message
         * @param senderType     type of message sender
         * @param content        message content
         * @param sentiment      sentiment associated with the message
         * @param sentimentScore numerical sentiment score associated with the message
         */
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
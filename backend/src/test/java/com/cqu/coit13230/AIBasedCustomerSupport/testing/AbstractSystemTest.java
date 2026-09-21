package com.cqu.coit13230.AIBasedCustomerSupport.testing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.ConversationStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.KnowledgeBaseEntry;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
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
import com.cqu.coit13230.AIBasedCustomerSupport.repository.NotificationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.SystemLogRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.security.JwtService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Provides the common Spring Boot setup and reusable helpers for system tests.
 * It resets test data before each test and creates standard users, tickets,
 * notifications, knowledge-base entries and authenticated HTTP requests.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.ai.provider=local",
                "app.ai.openai.api-key=",
                "app.seed.enabled=false"
        })
@ActiveProfiles("test")
public abstract class AbstractSystemTest {

    @Value("${local.server.port}")
    protected int port;

    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ConversationRepository conversationRepository;
    @Autowired protected TicketRepository ticketRepository;
    @Autowired protected MessageRepository messageRepository;
    @Autowired protected NotificationRepository notificationRepository;
    @Autowired protected KnowledgeBaseEntryRepository knowledgeBaseEntryRepository;
    @Autowired protected SystemLogRepository systemLogRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JwtService jwtService;

    protected final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    protected User admin;
    protected User agent;
    protected User agent2;
    protected User clientUser;
    protected User client2;
    protected User inactiveClient;

    /**
     * Clears persisted test data and recreates the standard users used by each system test.
     */
    @BeforeEach
    void resetDatabaseAndSeedUsers() {
        notificationRepository.deleteAll();
        systemLogRepository.deleteAll();
        messageRepository.deleteAll();
        ticketRepository.deleteAll();
        knowledgeBaseEntryRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();

        admin = saveUser("Admin", "admin@test.local", "Admin123!", UserRole.ADMIN, UserStatus.ACTIVE);
        agent = saveUser("Agent One", "agent1@test.local", "Agent123!", UserRole.AGENT, UserStatus.ACTIVE);
        agent2 = saveUser("Agent Two", "agent2@test.local", "Agent123!", UserRole.AGENT, UserStatus.ACTIVE);
        clientUser = saveUser("Client One", "client1@test.local", "Client123!", UserRole.CLIENT, UserStatus.ACTIVE);
        client2 = saveUser("Client Two", "client2@test.local", "Client123!", UserRole.CLIENT, UserStatus.ACTIVE);
        inactiveClient = saveUser("Inactive Client", "inactive@test.local", "Client123!", UserRole.CLIENT, UserStatus.INACTIVE);
    }

    /**
     * Creates and saves a test user with an encoded password.
     *
     * @param name user display name
     * @param email user email address
     * @param password plain test password to encode
     * @param role application role for the user
     * @param status account status for the user
     * @return the saved user
     */
    protected User saveUser(String name, String email, String password, UserRole role, UserStatus status) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(status);
        return userRepository.save(user);
    }

    /**
     * Generates a JWT for the supplied test user.
     *
     * @param user user to authenticate
     * @return generated bearer token
     */
    protected String token(User user) {
        return jwtService.generateToken(user);
    }

    /**
     * Creates an active conversation for a customer.
     *
     * @param customer customer who owns the conversation
     * @return the saved conversation
     */
    protected Conversation createConversation(User customer) {
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(ConversationStatus.ACTIVE);
        return conversationRepository.save(conversation);
    }

    /**
     * Creates a ticket and applies the timestamps and conversation state expected for its status.
     *
     * @param customer customer who owns the ticket
     * @param assignedAgent agent assigned to the ticket, or {@code null}
     * @param status initial ticket status
     * @return the saved ticket
     */
    protected Ticket createTicket(User customer, User assignedAgent, TicketStatus status) {
        Conversation conversation = createConversation(customer);
        Ticket ticket = new Ticket();
        ticket.setConversation(conversation);
        ticket.setCustomer(customer);
        ticket.setAssignedAgent(assignedAgent);
        ticket.setTitle("System test ticket");
        ticket.setCategory(TicketCategory.TECHNICAL);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(status);
        ticket.setSentiment(Sentiment.NEUTRAL);
        ticket.setSentimentScore(0.0);
        ticket.setAiConfidenceScore(0.8);
        if (status == TicketStatus.ESCALATED) {
            ticket.setEscalatedAt(java.time.LocalDateTime.now());
        }
        if (status == TicketStatus.RESOLVED || status == TicketStatus.RESOLVED_BY_AI) {
            ticket.setResolvedAt(java.time.LocalDateTime.now());
            conversation.setStatus(ConversationStatus.COMPLETED);
            conversation.setEndedAt(java.time.LocalDateTime.now());
            conversationRepository.save(conversation);
        }
        if (status == TicketStatus.CLOSED) {
            ticket.setResolvedAt(java.time.LocalDateTime.now());
            ticket.setClosedAt(java.time.LocalDateTime.now());
            conversation.setStatus(ConversationStatus.COMPLETED);
            conversation.setEndedAt(java.time.LocalDateTime.now());
            conversationRepository.save(conversation);
        }
        return ticketRepository.save(ticket);
    }

    /**
     * Creates a knowledge-base entry for AI matching tests.
     *
     * @param pattern question pattern to match
     * @param answer answer returned for the pattern
     * @param category knowledge-base category
     * @param active whether the entry is active
     * @return the saved knowledge-base entry
     */
    protected KnowledgeBaseEntry createKnowledge(String pattern, String answer, String category, boolean active) {
        KnowledgeBaseEntry entry = new KnowledgeBaseEntry();
        entry.setQuestionPattern(pattern);
        entry.setAnswerTemplate(answer);
        entry.setCategory(category);
        entry.setActive(active);
        entry.setLastUpdatedBy(admin);
        return knowledgeBaseEntryRepository.save(entry);
    }

    /**
     * Creates an unread notification linked to a user and ticket.
     *
     * @param user notification owner
     * @param ticket related ticket
     * @param message notification text
     * @return the saved notification
     */
    protected Notification createNotification(User user, Ticket ticket, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTicket(ticket);
        notification.setMessage(message);
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }

    /**
     * Sends an authenticated GET request to the test server.
     *
     * @param path API path
     * @param bearerToken JWT token, or {@code null} for a public request
     * @return captured API response
     * @throws Exception if the HTTP request cannot be completed
     */
    protected ApiResponse get(String path, String bearerToken) throws Exception {
        return request("GET", path, bearerToken, null, "application/json");
    }

    /**
     * Sends an authenticated DELETE request to the test server.
     *
     * @param path API path
     * @param bearerToken JWT token
     * @return captured API response
     * @throws Exception if the HTTP request cannot be completed
     */
    protected ApiResponse delete(String path, String bearerToken) throws Exception {
        return request("DELETE", path, bearerToken, null, "application/json");
    }

    /**
     * Sends a JSON POST request to the test server.
     *
     * @param path API path
     * @param bearerToken JWT token, or {@code null} when authentication is not required
     * @param body request object to serialize as JSON
     * @return captured API response
     * @throws Exception if serialization or the HTTP request fails
     */
    protected ApiResponse post(String path, String bearerToken, Object body) throws Exception {
        return request("POST", path, bearerToken, objectMapper.writeValueAsString(body), "application/json");
    }

    /**
     * Sends a JSON PUT request to the test server.
     *
     * @param path API path
     * @param bearerToken JWT token
     * @param body request object to serialize as JSON
     * @return captured API response
     * @throws Exception if serialization or the HTTP request fails
     */
    protected ApiResponse put(String path, String bearerToken, Object body) throws Exception {
        return request("PUT", path, bearerToken, objectMapper.writeValueAsString(body), "application/json");
    }

    /**
     * Sends a JSON PATCH request to the test server.
     *
     * @param path API path
     * @param bearerToken JWT token
     * @param body request object to serialize as JSON
     * @return captured API response
     * @throws Exception if serialization or the HTTP request fails
     */
    protected ApiResponse patch(String path, String bearerToken, Object body) throws Exception {
        return request("PATCH", path, bearerToken, objectMapper.writeValueAsString(body), "application/json");
    }

    /**
     * Sends a raw JSON request without converting the body from an object first.
     *
     * @param method HTTP method
     * @param path API path
     * @param bearerToken JWT token, or {@code null}
     * @param body raw request body
     * @return captured API response
     * @throws Exception if the HTTP request cannot be completed
     */
    protected ApiResponse requestRaw(String method, String path, String bearerToken, String body) throws Exception {
        return request(method, path, bearerToken, body, "application/json");
    }

    /**
     * Builds and sends an HTTP request and attempts to parse a JSON response body.
     *
     * @param method HTTP method
     * @param path API path
     * @param bearerToken JWT token, or {@code null}
     * @param body raw request body, or {@code null}
     * @param contentType request content type
     * @return response status, body, parsed JSON and headers
     * @throws Exception if the HTTP request cannot be completed
     */
    protected ApiResponse request(String method, String path, String bearerToken, String body, String contentType)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        if (body != null) {
            builder.header("Content-Type", contentType);
        }

        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        builder.method(method, publisher);

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        JsonNode json = null;
        if (response.body() != null && !response.body().isBlank()) {
            try {
                json = objectMapper.readTree(response.body());
            } catch (Exception ignored) {
                // Some security responses can be empty or non-JSON.
            }
        }
        return new ApiResponse(response.statusCode(), response.body(), json, response.headers().map());
    }

    /**
     * Logs in through the authentication endpoint and returns the issued token.
     *
     * @param email account email
     * @param password account password
     * @return JWT returned by the login endpoint
     * @throws Exception if the login request fails
     */
    protected String login(String email, String password) throws Exception {
        ApiResponse response = post("/api/auth/login", null, Map.of("email", email, "password", password));
        assertNotNull(response.json(), "Login response should be JSON");
        return response.json().path("token").asText();
    }

    /**
     * Stores the main parts of an HTTP response used by system-test assertions.
     *
     * @param status HTTP status code
     * @param body raw response body
     * @param json parsed JSON body when available
     * @param headers response headers
     */
    protected record ApiResponse(int status, String body, JsonNode json, Map<String, java.util.List<String>> headers) {
        /**
         * Returns the first value for a response header using a case-insensitive name match.
         *
         * @param name header name
         * @return first matching value, or {@code null} when absent
         */
        String header(String name) {
            return headers.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(name))
                    .flatMap(e -> e.getValue().stream())
                    .findFirst()
                    .orElse(null);
        }
    }
}

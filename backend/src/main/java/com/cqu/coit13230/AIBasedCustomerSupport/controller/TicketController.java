package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketMessageRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.service.TicketService;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

/**
 * Handles general ticket endpoints shared across user roles.
 *
 * <p>
 * This controller provides REST API operations for retrieving, creating,
 * updating, deleting, escalating, and adding messages to support tickets.
 * It also provides endpoints that allow the current user to retrieve their
 * own ticket information and ticket summary.
 * </p>
 *
 * <p>
 * Ticket operations are primarily delegated to the {@link TicketService},
 * while the {@link UserService} is used to determine the currently
 * authenticated user when role-specific processing is required.
 * </p>
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    /**
     * Creates the ticket controller with the required ticket service,
     * user service, and object mapper.
     *
     * @param ticketService service responsible for ticket-related operations
     * @param userService   service responsible for retrieving information
     *                      about the current user
     * @param objectMapper  object mapper used to convert incoming JSON data
     *                      into the required request or model objects
     */
    public TicketController(

            TicketService ticketService,

            UserService userService,

            ObjectMapper objectMapper) {

        this.ticketService = ticketService;

        this.userService = userService;

        this.objectMapper = objectMapper;

    }

    /**
     * Retrieves all tickets stored in the system.
     *
     * @return a list containing all tickets
     */
    @GetMapping
    public List<Ticket> getAllTickets() {

        return ticketService.getAllTickets();

    }

    /**
     * Retrieves ticket summaries belonging to the currently authenticated user.
     *
     * @return a list containing ticket summaries for the current user
     */
    @GetMapping("/my")
    public List<TicketSummaryResponse> getMyTickets() {

        return ticketService.getMyTicketSummaries();

    }

    /**
     * Retrieves a summary of ticket counts for the currently authenticated user.
     *
     * <p>
     * The returned map contains the total number of tickets belonging to
     * the current user and the number of those tickets that are currently
     * considered open.
     * </p>
     *
     * @return a map containing the current user's total and open ticket counts
     */
    @GetMapping("/my/summary")
    public Map<String, Long> getMyTicketSummary() {

        return Map.of(

                "total", ticketService.getMyTotalCount(),

                "open", ticketService.getMyOpenCount());

    }

    /**
     * Retrieves detailed information about a ticket that is accessible
     * to the current user.
     *
     * @param ticketId unique identifier of the ticket to retrieve
     * @return a response containing the accessible ticket details
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailsResponse> getTicketById(@PathVariable Long ticketId) {

        return ResponseEntity.ok(ticketService.getAccessibleTicketDetails(ticketId));

    }

    /**
     * Creates a new support ticket using the supplied JSON request body.
     *
     * <p>
     * This endpoint supports both manual ticket data and chat-style message
     * requests. This keeps older frontend requests working while the newer
     * chat flow can use the same endpoint.
     * </p>
     *
     * <p>
     * If the currently authenticated user has the {@link UserRole#CLIENT}
     * role, the request body is converted into a {@link CreateTicketRequest}
     * and a manual ticket is created. For other roles, the request body is
     * converted directly into a {@link Ticket} and saved.
     * </p>
     *
     * @param body JSON request body containing the ticket information
     * @return a response containing the newly created ticket information
     */
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody JsonNode body) {

        User current = userService.currentUser();

        if (current.getRole() == UserRole.CLIENT) {

            CreateTicketRequest request = objectMapper.convertValue(body, CreateTicketRequest.class);

            return ResponseEntity

                    .status(HttpStatus.CREATED)

                    .body(ticketService.createManualTicket(request));

        }

        Ticket ticket = objectMapper.convertValue(body, Ticket.class);

        return ResponseEntity

                .status(HttpStatus.CREATED)

                .body(ticketService.saveTicket(ticket));

    }

    /**
     * Updates an existing ticket identified by its ticket ID.
     *
     * <p>
     * The existing ticket is first retrieved to confirm that the record
     * exists. The supplied ticket is then assigned the same identifier
     * before being saved through the ticket service.
     * </p>
     *
     * @param ticketId unique identifier of the ticket to update
     * @param ticket   validated ticket information containing the updated values
     * @return a response containing the updated ticket
     */
    @PutMapping("/{ticketId}")
    public ResponseEntity<Ticket> updateTicket(

            @PathVariable Long ticketId,

            @Valid @RequestBody Ticket ticket) {

        ticketService.getTicketById(ticketId);

        ticket.setTicketId(ticketId);

        return ResponseEntity.ok(ticketService.saveTicket(ticket));

    }

    /**
     * Deletes an existing ticket using its unique identifier.
     *
     * <p>
     * After successful deletion, the endpoint returns an HTTP 204
     * No Content response.
     * </p>
     *
     * @param ticketId unique identifier of the ticket to delete
     * @return an empty response indicating successful deletion
     */
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {

        ticketService.deleteTicket(ticketId);

        return ResponseEntity.noContent().build();

    }

    /**
     * Escalates an existing support ticket.
     *
     * <p>
     * The ticket identifier is passed to the ticket service, which performs
     * the escalation operation and returns the updated ticket.
     * </p>
     *
     * @param ticketId unique identifier of the ticket to escalate
     * @return a response containing the escalated ticket
     */
    @PutMapping("/{ticketId}/escalate")
    public ResponseEntity<Ticket> escalateTicket(@PathVariable Long ticketId) {

        return ResponseEntity.ok(ticketService.escalateTicket(ticketId));

    }

    /**
     * Adds a manual message to an existing support ticket.
     *
     * <p>
     * The ticket identifier determines the ticket that receives the
     * message, while the request body contains the message information
     * to be processed by the ticket service.
     * </p>
     *
     * @param ticketId unique identifier of the ticket receiving the message
     * @param request  request containing the message information to add
     * @return a response containing the updated ticket details
     */
    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<TicketDetailsResponse> addMessage(

            @PathVariable Long ticketId,

            @RequestBody TicketMessageRequest request) {

        return ResponseEntity.ok(ticketService.addManualMessage(ticketId, request));

    }

}
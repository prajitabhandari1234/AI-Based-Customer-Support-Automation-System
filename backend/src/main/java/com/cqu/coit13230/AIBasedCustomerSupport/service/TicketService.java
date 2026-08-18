package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;

/**
 * Service class responsible for managing {@link Ticket} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting customer support tickets through the
 * {@link TicketRepository}.
 * </p>
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    /**
     * Constructs a new {@code TicketService} with the required
     * ticket repository dependency.
     *
     * @param ticketRepository repository used to access ticket data
     */
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Creates or updates a ticket.
     *
     * @param ticket the ticket to be saved
     * @return the saved ticket
     */
    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Retrieves all tickets.
     *
     * @return a list of all tickets
     */
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Retrieves a ticket by identifier.
     *
     * @param ticketId the identifier of the ticket
     * @return an optional containing the ticket if found
     */
    public Optional<Ticket> getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    /**
     * Deletes a ticket by identifier.
     *
     * @param ticketId the identifier of the ticket to delete
     */
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }
}
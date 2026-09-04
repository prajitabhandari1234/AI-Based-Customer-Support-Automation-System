package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

/**
 * Repository interface for managing {@link Ticket} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * customer support ticket records through Spring Data JPA.
 * </p>
 *
 * <p>
 * The repository also provides customer-specific, status-based,
 * and analytics queries used by customer, support-agent,
 * and administrator workflows.
 * </p>
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Retrieves all support tickets belonging to the specified customer,
     * ordered from the most recently created ticket to the oldest.
     *
     * @param customerId unique identifier of the customer
     * @return list of tickets belonging to the specified customer
     */
    List<Ticket> findByCustomerUserIdOrderByCreatedAtDesc(
            Long customerId);

    /**
     * Retrieves all tickets with the specified status,
     * ordered from the oldest ticket to the newest.
     *
     * <p>
     * This query is used by support agents to retrieve tickets
     * that require human intervention, such as escalated tickets.
     * </p>
     *
     * @param status lifecycle status used to filter tickets
     * @return list of tickets matching the specified status
     */
    List<Ticket> findByStatusOrderByCreatedAtAsc(
            TicketStatus status);

    /**
     * Counts tickets with the specified lifecycle status.
     *
     * @param status lifecycle status to count
     * @return number of tickets with the specified status
     */
    long countByStatus(TicketStatus status);

    /**
     * Counts tickets with the specified priority level.
     *
     * @param priority priority level to count
     * @return number of tickets with the specified priority
     */
    long countByPriority(TicketPriority priority);

    /**
     * Counts tickets belonging to the specified category.
     *
     * @param category ticket category to count
     * @return number of tickets belonging to the specified category
     */
    long countByCategory(TicketCategory category);
}
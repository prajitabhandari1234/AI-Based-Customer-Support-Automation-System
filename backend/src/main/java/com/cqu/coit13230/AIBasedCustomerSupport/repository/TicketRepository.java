package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

/**
 * Provides database access and query operations for {@link Ticket}
 * entities within the AI-based customer support system.
 *
 * <p>
 * This repository extends Spring Data JPA's {@link JpaRepository}
 * to provide standard persistence operations and
 * {@link JpaSpecificationExecutor} to support dynamic and
 * specification-based ticket queries.
 * </p>
 *
 * <p>
 * It also defines project-specific query methods for retrieving
 * tickets by customer, status, assigned agent, and creation date,
 * as well as counting tickets for customer ticket summaries.
 * </p>
 */
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    /**
     * Retrieves all tickets belonging to a specific customer,
     * ordered by their creation time in descending order.
     *
     * <p>
     * The most recently created tickets are returned first.
     * </p>
     *
     * @param customerId unique identifier of the customer whose
     *                   tickets are requested
     * @return a list of the customer's tickets ordered from the
     *         most recently created to the oldest
     */
    List<Ticket> findByCustomerUserIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Retrieves all tickets with the specified status, ordered by
     * their creation time in ascending order.
     *
     * <p>
     * The oldest matching tickets are returned first.
     * </p>
     *
     * @param status ticket status used to filter the results
     * @return a list of tickets with the specified status ordered
     *         from the oldest to the most recently created
     */
    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);

    /**
     * Retrieves all tickets assigned to a specific support agent,
     * ordered by their most recent update time in descending order.
     *
     * <p>
     * The most recently updated tickets are returned first.
     * </p>
     *
     * @param agentId unique identifier of the assigned support agent
     * @return a list of tickets assigned to the specified agent,
     *         ordered from the most recently updated to the oldest
     */
    List<Ticket> findByAssignedAgentUserIdOrderByUpdatedAtDesc(Long agentId);

    /**
     * Retrieves tickets created between the specified start and end
     * date and time values.
     *
     * <p>
     * Matching tickets are ordered by their creation time in
     * descending order so that the most recently created ticket
     * appears first.
     * </p>
     *
     * @param start beginning of the creation date and time range
     * @param end   end of the creation date and time range
     * @return a list of tickets created within the specified range,
     *         ordered from the most recently created to the oldest
     */
    List<Ticket> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Counts the total number of tickets belonging to a specific customer.
     *
     * @param customerId unique identifier of the customer
     * @return total number of tickets associated with the customer
     */
    long countByCustomerUserId(Long customerId);

    /**
     * Counts the number of tickets belonging to a specific customer
     * whose status is included in the supplied collection.
     *
     * @param customerId unique identifier of the customer
     * @param statuses   collection of ticket statuses included in the count
     * @return number of customer tickets matching any of the supplied statuses
     */
    long countByCustomerUserIdAndStatusIn(Long customerId, Collection<TicketStatus> statuses);

}
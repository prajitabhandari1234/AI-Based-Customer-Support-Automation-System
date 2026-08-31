package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;

/**
 * Repository interface for managing {@link Ticket} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * customer support ticket records through Spring Data JPA.
 * </p>
 *
 * <p>
 * The repository also provides customer-specific queries used
 * to retrieve the support ticket history of an authenticated
 * customer.
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
    List<Ticket> findByCustomerUserIdOrderByCreatedAtDesc(Long customerId);
}
package com.cqu.coit13230.AIBasedCustomerSupport.repository;

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
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
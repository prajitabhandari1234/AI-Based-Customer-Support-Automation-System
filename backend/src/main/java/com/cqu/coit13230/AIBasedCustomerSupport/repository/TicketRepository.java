package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

/**
 * Provides database access and ticket lookup queries.
 * Spring Data JPA provides the standard CRUD operations while this interface adds project-specific lookups.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByCustomerUserIdOrderByCreatedAtDesc(Long customerId);

    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);

    List<Ticket> findByAssignedAgentUserIdOrderByUpdatedAtDesc(Long agentId);

    List<Ticket> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    long countByCustomerUserId(Long customerId);

    long countByCustomerUserIdAndStatusIn(Long customerId, Collection<TicketStatus> statuses);
}

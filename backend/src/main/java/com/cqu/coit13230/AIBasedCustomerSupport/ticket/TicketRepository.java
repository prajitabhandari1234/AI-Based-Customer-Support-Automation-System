package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerOrderByUpdatedAtDesc(User customer);
    List<Ticket> findByAssignedAgentOrderByPriorityDescUpdatedAtDesc(User agent);
    List<Ticket> findAllByOrderByPriorityDescUpdatedAtDesc();
    long countByCustomer(User customer);
    long countByCustomerAndStatusIn(User customer, List<TicketStatus> statuses);
    List<Ticket> findByCreatedAtAfterOrderByCreatedAtAsc(Instant from);
}

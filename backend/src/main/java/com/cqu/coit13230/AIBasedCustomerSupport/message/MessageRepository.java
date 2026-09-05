package com.cqu.coit13230.AIBasedCustomerSupport.message;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    long countByTicketAndSenderType(Ticket ticket, SenderType senderType);
}

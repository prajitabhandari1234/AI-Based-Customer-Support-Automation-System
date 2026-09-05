package com.cqu.coit13230.AIBasedCustomerSupport.log;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "system_logs", indexes = @Index(name = "idx_log_created", columnList = "created_at"))
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false, length = 1000)
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public Instant getCreatedAt() { return createdAt; }
}

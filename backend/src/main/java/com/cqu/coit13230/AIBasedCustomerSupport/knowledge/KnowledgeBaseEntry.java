package com.cqu.coit13230.AIBasedCustomerSupport.knowledge;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "knowledge_base_entries")
public class KnowledgeBaseEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String questionPattern;

    @Lob
    @Column(nullable = false)
    private String answerTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketCategory category;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;

    @Column(nullable = false)
    private Instant lastUpdatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() { lastUpdatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getQuestionPattern() { return questionPattern; }
    public void setQuestionPattern(String questionPattern) { this.questionPattern = questionPattern; }
    public String getAnswerTemplate() { return answerTemplate; }
    public void setAnswerTemplate(String answerTemplate) { this.answerTemplate = answerTemplate; }
    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public User getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(User lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
}

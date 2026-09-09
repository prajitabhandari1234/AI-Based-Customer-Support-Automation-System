package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;

/**
 * Provides database access for conversation messages.
 * Spring Data JPA provides the standard CRUD operations while this interface adds project-specific lookups.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationConversationIdOrderByCreatedAtAsc(Long conversationId);
}

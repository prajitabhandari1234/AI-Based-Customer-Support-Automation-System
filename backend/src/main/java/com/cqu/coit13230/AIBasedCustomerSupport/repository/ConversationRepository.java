package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;

/**
 * Provides database access for conversations.
 * Spring Data JPA provides the standard CRUD operations while this interface adds project-specific lookups.
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByCustomerUserIdOrderByStartedAtDesc(Long customerId);
}

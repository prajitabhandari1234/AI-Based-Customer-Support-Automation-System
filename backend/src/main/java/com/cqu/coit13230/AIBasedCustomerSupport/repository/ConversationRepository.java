package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;

/**
 * Repository interface for managing {@link Conversation} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * customer conversation records through Spring Data JPA.
 * </p>
 */
@Repository
public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

}
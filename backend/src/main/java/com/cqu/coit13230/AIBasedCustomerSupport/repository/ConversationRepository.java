package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;

/**
 * Provides database access operations for {@link Conversation} entities.
 *
 * <p>
 * This repository extends Spring Data JPA's {@link JpaRepository},
 * which provides standard persistence operations such as creating,
 * retrieving, updating, and deleting conversation records.
 * </p>
 *
 * <p>
 * It also defines project-specific query methods for retrieving
 * conversations associated with a particular customer.
 * </p>
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Retrieves all conversations belonging to a specific customer,
     * ordered by their start time in descending order.
     *
     * <p>
     * The most recently started conversations are returned first.
     * Spring Data JPA automatically derives the database query from
     * the method name.
     * </p>
     *
     * @param customerId unique identifier of the customer whose
     *                   conversations are requested
     * @return a list of the customer's conversations ordered from
     *         the most recently started to the oldest
     */
    List<Conversation> findByCustomerUserIdOrderByStartedAtDesc(Long customerId);

}
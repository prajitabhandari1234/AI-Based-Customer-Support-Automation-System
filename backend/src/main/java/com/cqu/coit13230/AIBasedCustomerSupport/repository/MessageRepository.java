package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;

/**
 * Provides database access operations for {@link Message} entities
 * within the AI-based customer support system.
 *
 * <p>
 * This repository extends Spring Data JPA's {@link JpaRepository},
 * which provides standard persistence operations such as creating,
 * retrieving, updating, and deleting conversation messages.
 * </p>
 *
 * <p>
 * It also defines a project-specific query method for retrieving
 * messages associated with a particular conversation in chronological
 * order.
 * </p>
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Retrieves all messages associated with a specific conversation,
     * ordered by their creation time in ascending order.
     *
     * <p>
     * This ordering returns the oldest message first and the most
     * recently created message last, allowing the conversation history
     * to be displayed in chronological order. Spring Data JPA
     * automatically derives the database query from the method name.
     * </p>
     *
     * @param conversationId unique identifier of the conversation whose
     *                       messages are requested
     * @return a list of messages associated with the conversation,
     *         ordered by creation time in ascending order
     */
    List<Message> findByConversationConversationIdOrderByCreatedAtAsc(Long conversationId);

}
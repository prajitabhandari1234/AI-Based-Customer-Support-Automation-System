package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;

/**
 * Repository interface for managing {@link Message} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * conversation message records through Spring Data JPA.
 * </p>
 *
 * <p>
 * The repository also provides conversation-specific queries used
 * to retrieve message history in chronological order.
 * </p>
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Retrieves all messages associated with the specified conversation,
     * ordered from the oldest message to the newest.
     *
     * @param conversationId unique identifier of the conversation
     * @return ordered list of messages belonging to the conversation
     */
    List<Message> findByConversationConversationIdOrderByCreatedAtAsc(
            Long conversationId);
}
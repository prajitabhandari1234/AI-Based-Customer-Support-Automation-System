package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;

/**
 * Provides database access operations for {@link Notification} entities
 * within the AI-based customer support system.
 *
 * <p>
 * This repository extends Spring Data JPA's {@link JpaRepository},
 * which provides standard persistence operations such as creating,
 * retrieving, updating, and deleting notification records.
 * </p>
 *
 * <p>
 * It also defines a project-specific query method for retrieving
 * notifications associated with a particular user in reverse
 * chronological order.
 * </p>
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Retrieves all notifications associated with a specific user,
     * ordered by their creation time in descending order.
     *
     * <p>
     * This ordering returns the most recently created notification
     * first. Spring Data JPA automatically derives the required
     * database query from the method name.
     * </p>
     *
     * @param userId unique identifier of the user whose notifications
     *               are requested
     * @return a list of notifications associated with the user,
     *         ordered from the most recently created to the oldest
     */
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId);

}
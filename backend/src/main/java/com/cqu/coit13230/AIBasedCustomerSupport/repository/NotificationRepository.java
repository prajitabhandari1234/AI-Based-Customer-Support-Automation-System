package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;

/**
 * Repository interface for managing {@link Notification} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * notification records through Spring Data JPA.
 * </p>
 *
 * <p>
 * Customer-specific queries are also provided to retrieve
 * notifications belonging to a particular user in reverse
 * chronological order.
 * </p>
 */
@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /**
     * Retrieves all notifications belonging to the specified user,
     * ordered from newest to oldest.
     *
     * @param userId unique identifier of the notification recipient
     * @return notifications belonging to the specified user
     */
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(
            Long userId);
}
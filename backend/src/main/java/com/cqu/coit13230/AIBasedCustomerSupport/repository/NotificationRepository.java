package com.cqu.coit13230.AIBasedCustomerSupport.repository;

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
 */
@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

}
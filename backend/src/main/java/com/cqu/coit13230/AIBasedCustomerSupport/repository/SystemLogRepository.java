package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;

/**
 * Repository interface for managing {@link SystemLog} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for
 * system log records through Spring Data JPA.
 * </p>
 */
@Repository
public interface SystemLogRepository
        extends JpaRepository<SystemLog, Long> {

}
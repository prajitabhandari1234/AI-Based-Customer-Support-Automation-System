package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;

/**
 * Provides database access for system logs.
 * Spring Data JPA provides the standard CRUD operations while this interface adds project-specific lookups.
 */
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    List<SystemLog> findAllByOrderByCreatedAtDesc();
}

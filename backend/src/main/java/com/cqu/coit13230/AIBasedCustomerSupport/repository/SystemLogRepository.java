package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;

/**
 * Provides database access operations for {@link SystemLog} entities
 * within the AI-based customer support system.
 *
 * <p>
 * This repository extends Spring Data JPA's {@link JpaRepository},
 * which provides standard persistence operations such as creating,
 * retrieving, updating, and deleting system log records.
 * </p>
 *
 * <p>
 * It also defines a project-specific query method for retrieving
 * system logs in reverse chronological order based on their
 * creation timestamp.
 * </p>
 */
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    /**
     * Retrieves all system log entries ordered by their creation
     * time in descending order.
     *
     * <p>
     * This ordering returns the most recently created system log
     * entry first. Spring Data JPA automatically derives the required
     * database query from the method name.
     * </p>
     *
     * @return a list of all system log entries ordered from the
     *         most recently created to the oldest
     */
    List<SystemLog> findAllByOrderByCreatedAtDesc();

}
package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;
import com.cqu.coit13230.AIBasedCustomerSupport.service.SystemLogService;

/**
 * Provides administrator access to stored system logs.
 *
 * <p>
 * This controller exposes read-only REST API endpoints that allow
 * administrators to view application activity records stored in the
 * system.
 * </p>
 *
 * <p>
 * System log retrieval operations are delegated to the
 * {@link SystemLogService}, which is responsible for accessing the
 * stored system log records.
 * </p>
 */
@RestController
@RequestMapping("/api/system-logs")
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * Creates the system log controller with the required system log service.
     *
     * @param systemLogService service responsible for retrieving
     *                         stored system log records
     */
    public SystemLogController(SystemLogService systemLogService) {

        this.systemLogService = systemLogService;

    }

    /**
     * Retrieves all system log records stored in the application.
     *
     * <p>
     * This endpoint provides administrators with a complete list of
     * available application activity records.
     * </p>
     *
     * @return a response containing the list of all stored system logs
     */
    @GetMapping
    public ResponseEntity<List<SystemLog>> getAllSystemLogs() {

        return ResponseEntity.ok(systemLogService.getAllSystemLogs());

    }

    /**
     * Retrieves a specific system log record using its unique identifier.
     *
     * <p>
     * The supplied system log identifier is passed to the system log
     * service to retrieve the corresponding activity record.
     * </p>
     *
     * @param systemLogId unique identifier of the system log to retrieve
     * @return a response containing the requested system log record
     */
    @GetMapping("/{systemLogId}")
    public ResponseEntity<SystemLog> getSystemLogById(@PathVariable Long systemLogId) {

        return ResponseEntity.ok(systemLogService.getSystemLogById(systemLogId));

    }

}
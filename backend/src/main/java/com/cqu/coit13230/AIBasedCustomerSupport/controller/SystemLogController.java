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
 * REST controller responsible for administrator access to
 * {@link SystemLog} records.
 *
 * <p>
 * System logs provide an audit trail of important application
 * activity including authentication attempts, ticket creation,
 * ticket escalation, and AI-related events.
 * </p>
 *
 * <p>
 * Audit records are read-only through the REST API. Log entries
 * are created automatically by backend services and cannot be
 * manually created, updated, or deleted through this controller.
 * </p>
 *
 * <p>
 * Access to these endpoints is restricted to administrators
 * through the application's security configuration.
 * </p>
 */
@RestController
@RequestMapping("/api/system-logs")
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * Constructs the system-log controller.
     *
     * @param systemLogService service used to retrieve system logs
     */
    public SystemLogController(
            SystemLogService systemLogService) {

        this.systemLogService = systemLogService;
    }

    /**
     * Retrieves all system log records.
     *
     * @return list containing all system log records
     */
    @GetMapping
    public List<SystemLog> getAllSystemLogs() {

        return systemLogService.getAllSystemLogs();
    }

    /**
     * Retrieves a system log record by identifier.
     *
     * @param systemLogId identifier of the requested system log
     * @return requested system log or HTTP 404 when not found
     */
    @GetMapping("/{systemLogId}")
    public ResponseEntity<SystemLog> getSystemLogById(
            @PathVariable Long systemLogId) {

        return systemLogService
                .getSystemLogById(systemLogId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}
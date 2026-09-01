package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;
import com.cqu.coit13230.AIBasedCustomerSupport.service.SystemLogService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link SystemLog} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting system log records through the {@link SystemLogService}.
 * </p>
 */
@RestController
@RequestMapping("/api/system-logs")
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * Constructs a new {@code SystemLogController} with the required
     * system log service.
     *
     * @param systemLogService service used to manage system log operations
     */
    public SystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    /**
     * Retrieves all system log records.
     *
     * @return a list of all system log records
     */
    @GetMapping
    public List<SystemLog> getAllSystemLogs() {
        return systemLogService.getAllSystemLogs();
    }

    /**
     * Retrieves a system log record by identifier.
     *
     * @param systemLogId the identifier of the system log record
     * @return the requested system log, or HTTP 404 if not found
     */
    @GetMapping("/{systemLogId}")
    public ResponseEntity<SystemLog> getSystemLogById(
            @PathVariable Long systemLogId) {

        return systemLogService.getSystemLogById(systemLogId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new system log record.
     *
     * @param systemLog the system log record to create
     * @return the created system log record
     */
    @PostMapping
    public SystemLog createSystemLog(
            @Valid @RequestBody SystemLog systemLog) {

        return systemLogService.saveSystemLog(systemLog);
    }

    /**
     * Updates an existing system log record.
     *
     * @param systemLogId the identifier of the system log record to update
     * @param systemLog the updated system log information
     * @return the updated system log, or HTTP 404 if not found
     */
    @PutMapping("/{systemLogId}")
    public ResponseEntity<SystemLog> updateSystemLog(
            @PathVariable Long systemLogId,
            @Valid @RequestBody SystemLog systemLog) {

        return systemLogService.getSystemLogById(systemLogId)
                .map(existingSystemLog -> {

                    existingSystemLog.setUser(systemLog.getUser());
                    existingSystemLog.setTicket(systemLog.getTicket());
                    existingSystemLog.setEventType(systemLog.getEventType());
                    existingSystemLog.setDescription(systemLog.getDescription());

                    return ResponseEntity.ok(
                            systemLogService.saveSystemLog(existingSystemLog));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a system log record by identifier.
     *
     * @param systemLogId the identifier of the system log record to delete
     * @return HTTP 204 if deleted, or HTTP 404 if not found
     */
    @DeleteMapping("/{systemLogId}")
    public ResponseEntity<Void> deleteSystemLog(
            @PathVariable Long systemLogId) {

        if (systemLogService.getSystemLogById(systemLogId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        systemLogService.deleteSystemLog(systemLogId);

        return ResponseEntity.noContent().build();
    }
}
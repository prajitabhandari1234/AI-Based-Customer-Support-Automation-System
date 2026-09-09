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
 * Provides admin access to stored system logs.
 * This gives admins a simple API view of application activity records.
 */
@RestController
@RequestMapping("/api/system-logs")
public class SystemLogController {

    private final SystemLogService systemLogService;

    public SystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @GetMapping
    public ResponseEntity<List<SystemLog>> getAllSystemLogs() {
        return ResponseEntity.ok(systemLogService.getAllSystemLogs());
    }

    @GetMapping("/{systemLogId}")
    public ResponseEntity<SystemLog> getSystemLogById(@PathVariable Long systemLogId) {
        return ResponseEntity.ok(systemLogService.getSystemLogById(systemLogId));
    }
}

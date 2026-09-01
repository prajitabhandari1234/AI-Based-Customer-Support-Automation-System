package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.SystemLogRepository;

/**
 * Service class responsible for managing {@link SystemLog} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting system log records through the
 * {@link SystemLogRepository}.
 * </p>
 */
@Service
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    /**
     * Constructs a new {@code SystemLogService} with the required
     * system log repository dependency.
     *
     * @param systemLogRepository repository used to access system log data
     */
    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Creates or updates a system log record.
     *
     * @param systemLog the system log record to be saved
     * @return the saved system log record
     */
    public SystemLog saveSystemLog(SystemLog systemLog) {
        return systemLogRepository.save(systemLog);
    }

    /**
     * Retrieves all system log records.
     *
     * @return a list of all system log records
     */
    public List<SystemLog> getAllSystemLogs() {
        return systemLogRepository.findAll();
    }

    /**
     * Retrieves a system log record by identifier.
     *
     * @param systemLogId the identifier of the system log record
     * @return an optional containing the system log record if found
     */
    public Optional<SystemLog> getSystemLogById(Long systemLogId) {
        return systemLogRepository.findById(systemLogId);
    }

    /**
     * Deletes a system log record by identifier.
     *
     * @param systemLogId the identifier of the system log record to delete
     */
    public void deleteSystemLog(Long systemLogId) {
        systemLogRepository.deleteById(systemLogId);
    }
}
package com.cqu.coit13230.AIBasedCustomerSupport.log;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemLogService {
    private final SystemLogRepository logs;

    public SystemLogService(SystemLogRepository logs) {
        this.logs = logs;
    }

    @Transactional
    public void record(String eventType, String details, User user, Ticket ticket) {
        SystemLog log = new SystemLog();
        log.setEventType(eventType);
        log.setDetails(details);
        log.setUser(user);
        log.setTicket(ticket);
        logs.save(log);
    }
}

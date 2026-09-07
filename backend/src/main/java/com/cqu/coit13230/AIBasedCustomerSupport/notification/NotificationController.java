package com.cqu.coit13230.AIBasedCustomerSupport.notification;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationView> mine() {
        return service.mine();
    }

    @PatchMapping("/{id}/read")
    public NotificationView markRead(@PathVariable Long id) {
        return service.markRead(id);
    }
}

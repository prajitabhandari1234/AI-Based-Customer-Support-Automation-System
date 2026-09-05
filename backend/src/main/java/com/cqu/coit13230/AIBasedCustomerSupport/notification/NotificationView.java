package com.cqu.coit13230.AIBasedCustomerSupport.notification;

import java.time.Instant;

public record NotificationView(
        Long id,
        Long ticketId,
        String message,
        boolean read,
        Instant createdAt
) {
    public static NotificationView from(Notification notification) {
        return new NotificationView(
                notification.getId(),
                notification.getTicket() == null ? null : notification.getTicket().getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

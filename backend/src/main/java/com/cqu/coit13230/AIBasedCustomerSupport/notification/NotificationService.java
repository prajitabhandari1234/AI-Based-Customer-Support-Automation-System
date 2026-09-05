package com.cqu.coit13230.AIBasedCustomerSupport.notification;

import com.cqu.coit13230.AIBasedCustomerSupport.common.ForbiddenException;
import com.cqu.coit13230.AIBasedCustomerSupport.common.NotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import com.cqu.coit13230.AIBasedCustomerSupport.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final UserService users;

    public NotificationService(NotificationRepository notifications, UserService users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional
    public void send(User user, Ticket ticket, String message) {
        if (user == null) return;
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTicket(ticket);
        notification.setMessage(message);
        notifications.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationView> mine() {
        return notifications.findByUserOrderByCreatedAtDesc(users.currentUser()).stream()
                .map(NotificationView::from).toList();
    }

    @Transactional
    public NotificationView markRead(Long id) {
        User current = users.currentUser();
        Notification notification = notifications.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(current.getId())) {
            throw new ForbiddenException("You cannot change this notification");
        }
        notification.setRead(true);
        return NotificationView.from(notifications.save(notification));
    }
}

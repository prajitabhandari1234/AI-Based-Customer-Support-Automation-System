package com.cqu.coit13230.AIBasedCustomerSupport.notification;

import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}

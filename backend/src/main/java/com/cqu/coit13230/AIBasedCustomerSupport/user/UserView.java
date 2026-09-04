package com.cqu.coit13230.AIBasedCustomerSupport.user;

import java.time.Instant;

public record UserView(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status,
        Instant createdAt
) {
    public static UserView from(User user) {
        return new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus(), user.getCreatedAt());
    }
}

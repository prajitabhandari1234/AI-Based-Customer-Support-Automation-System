package com.cqu.coit13230.AIBasedCustomerSupport.user;

public record UpdateUserRequest(
        String name,
        Role role,
        UserStatus status
) {}

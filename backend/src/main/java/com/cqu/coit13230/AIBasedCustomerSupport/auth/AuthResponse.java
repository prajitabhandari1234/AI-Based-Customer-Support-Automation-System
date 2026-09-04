package com.cqu.coit13230.AIBasedCustomerSupport.auth;

import com.cqu.coit13230.AIBasedCustomerSupport.user.UserView;

public record AuthResponse(
        String token,
        UserView user
) {}

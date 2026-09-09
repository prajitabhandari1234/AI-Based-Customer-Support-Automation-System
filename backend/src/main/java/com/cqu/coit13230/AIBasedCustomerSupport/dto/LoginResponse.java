package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns the logged-in user details together with the JWT token.
 * The DTO keeps API response data separate from the database entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String message;
    private String token;
    private User user;

    // Builds the login response from the authenticated user.
    public static LoginResponse from(User user, String token, String message) {
        return new LoginResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                message,
                token,
                user);
    }
}

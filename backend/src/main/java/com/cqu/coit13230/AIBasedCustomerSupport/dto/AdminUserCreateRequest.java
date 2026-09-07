package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request object used by administrators to create user accounts.
 *
 * <p>
 * Administrators may create customer, support-agent, or administrator
 * accounts and specify the initial account status.
 * </p>
 *
 * <p>
 * The supplied password is never stored directly. It is securely hashed
 * by the service layer before the user account is persisted.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
public class AdminUserCreateRequest {

    /**
     * Full name of the user.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * Email address used to identify and authenticate the user.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email address must be valid")
    private String email;

    /**
     * Plain-text password supplied during account creation.
     *
     * <p>
     * The password is converted to a secure BCrypt hash before
     * being stored.
     * </p>
     */
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            message = "Password must contain at least 8 characters")
    private String password;

    /**
     * Role assigned to the new user.
     */
    @NotNull(message = "User role is required")
    private UserRole role;

    /**
     * Initial status assigned to the new user account.
     */
    @NotNull(message = "User status is required")
    private UserStatus status;
}
package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Data transfer object used by administrators to update
 * a user's role and account status.
 */
public class AdminUserUpdateRequest {

    /**
     * Role assigned to the user.
     */
    @NotNull(message = "User role is required")
    private UserRole role;

    /**
     * Current status of the user account.
     */
    @NotNull(message = "User status is required")
    private UserStatus status;

    /**
     * Creates an empty {@code AdminUserUpdateRequest}.
     */
    public AdminUserUpdateRequest() {
    }

    /**
     * Creates a new administrator user update request.
     *
     * @param role role assigned to the user
     * @param status status assigned to the user
     */
    public AdminUserUpdateRequest(
            UserRole role,
            UserStatus status) {

        this.role = role;
        this.status = status;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
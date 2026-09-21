package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries role and status changes made by an administrator.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate
 * user role and account status changes submitted through the
 * administrator user-management functionality.
 * </p>
 *
 * <p>
 * The validated request data is transferred to the service layer,
 * where the requested user account changes are processed.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest {

    /**
     * Role to be assigned to the user.
     *
     * <p>
     * The role is required and determines the user's role within
     * the application.
     * </p>
     */
    @NotNull
    private UserRole role;

    /**
     * Account status to be assigned to the user.
     *
     * <p>
     * The status is required and represents the current state of
     * the user's account.
     * </p>
     */
    @NotNull
    private UserStatus status;

}
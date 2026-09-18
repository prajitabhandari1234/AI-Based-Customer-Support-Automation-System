package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries user details submitted by an administrator when creating an account.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate the
 * information required to create a new user through the administrator
 * user-management functionality.
 * </p>
 *
 * <p>
 * The validated request data is transferred to the service layer, where
 * the user account creation process is handled.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserCreateRequest {

    /**
     * Name of the user being created.
     *
     * <p>
     * The name is required and must not exceed 120 characters.
     * </p>
     */
    @NotBlank
    @Size(max = 120)
    private String name;

    /**
     * Email address of the user being created.
     *
     * <p>
     * The email is required and must contain a valid email address format.
     * </p>
     */
    @NotBlank
    @Email
    private String email;

    /**
     * Password for the new user account.
     *
     * <p>
     * The password is required and must contain between 8 and
     * 100 characters.
     * </p>
     */
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    /**
     * Role assigned to the new user.
     *
     * <p>
     * The role is required and determines the user's role within
     * the application.
     * </p>
     */
    @NotNull
    private UserRole role;

    /**
     * Account status assigned to the new user.
     *
     * <p>
     * The status is required and represents the current state of
     * the user's account.
     * </p>
     */
    @NotNull
    private UserStatus status;

}
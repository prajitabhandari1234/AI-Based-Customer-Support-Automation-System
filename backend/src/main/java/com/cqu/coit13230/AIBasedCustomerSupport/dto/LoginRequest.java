package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries the email and password submitted during login.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate
 * authentication credentials submitted by a user when attempting
 * to log in to the customer support system.
 * </p>
 *
 * <p>
 * The validated email address and password are transferred to the
 * service layer, where the user's authentication request is processed.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Email address submitted by the user during login.
     *
     * <p>
     * The email address is required, must not be blank, and must
     * follow a valid email address format.
     * </p>
     */
    @NotBlank
    @Email
    private String email;

    /**
     * Password submitted by the user during login.
     *
     * <p>
     * The password is required and must not be blank.
     * </p>
     */
    @NotBlank
    private String password;

}
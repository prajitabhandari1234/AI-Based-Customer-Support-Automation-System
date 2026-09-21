package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries the details submitted when a customer registers.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate
 * customer information submitted during the registration process.
 * </p>
 *
 * <p>
 * The request contains the customer's name, email address, and password.
 * The validated registration data is transferred to the service layer,
 * where the customer account creation process is handled.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * Name of the customer registering for an account.
     *
     * <p>
     * The name is required, must not be blank, and must contain
     * between 2 and 120 characters.
     * </p>
     */
    @NotBlank
    @Size(min = 2, max = 120)
    private String name;

    /**
     * Email address submitted by the customer during registration.
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
     * Password submitted by the customer during registration.
     *
     * <p>
     * The password is required, must not be blank, and must contain
     * between 8 and 100 characters.
     * </p>
     */
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

}
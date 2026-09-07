package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the information required to register a new customer account
 * in the AI-Based Customer Support Automation System.
 *
 * <p>This data transfer object (DTO) is used to receive registration
 * information from the client. It contains only the information that a
 * customer is permitted to provide during account registration.</p>
 *
 * <p>User roles and account statuses are not included in this request
 * because they are assigned by the backend during the registration
 * process.</p>
 */
public class RegisterRequest {

    /**
     * Full name of the customer creating the account.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * Email address that will be associated with the customer account.
     *
     * <p>The email must be provided in a valid email format.</p>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * Plain-text password provided by the customer during registration.
     *
     * <p>The password must contain at least eight characters. It is
     * converted to a secure password hash before being stored in the
     * database.</p>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    /**
     * Constructs an empty registration request.
     *
     * <p>This constructor is required for JSON deserialization.</p>
     */
    public RegisterRequest() {
    }

    /**
     * Constructs a registration request with the supplied customer details.
     *
     * @param name full name of the customer
     * @param email email address of the customer
     * @param password plain-text password provided during registration
     */
    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the customer's full name.
     *
     * @return the customer's full name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the customer's full name.
     *
     * @param name the customer's full name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the customer's email address.
     *
     * @return the customer's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the customer's email address.
     *
     * @param email the customer's email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the plain-text password supplied during registration.
     *
     * @return the registration password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the plain-text password used during registration.
     *
     * @param password the registration password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
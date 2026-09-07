package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents the credentials required to authenticate a user
 * in the AI-Based Customer Support Automation System.
 *
 * <p>This data transfer object receives the user's email address
 * and plain-text password during the login process.</p>
 */
public class LoginRequest {

    /**
     * Email address associated with the user's account.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * Plain-text password supplied by the user during authentication.
     *
     * <p>The password is compared with the stored BCrypt password hash
     * and is never stored directly in the database.</p>
     */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Constructs an empty login request.
     */
    public LoginRequest() {
    }

    /**
     * Constructs a login request with the supplied credentials.
     *
     * @param email user's email address
     * @param password user's plain-text password
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the user's email address.
     *
     * @return user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email user's email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the password supplied during login.
     *
     * @return plain-text login password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password supplied during login.
     *
     * @param password plain-text login password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
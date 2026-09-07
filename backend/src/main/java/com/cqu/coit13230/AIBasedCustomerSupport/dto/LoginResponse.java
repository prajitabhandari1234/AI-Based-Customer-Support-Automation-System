package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

/**
 * Represents the response returned after a successful user login.
 *
 * <p>
 * This DTO exposes safe account information together with the JWT
 * generated after successful authentication. It does not expose the
 * user's password or password hash.
 * </p>
 */
public class LoginResponse {

    private Long userId;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String message;
    private String token;

    /**
     * Constructs an empty login response.
     */
    public LoginResponse() {
    }

    /**
     * Constructs a login response with authenticated user information
     * and the generated JWT.
     *
     * @param userId unique identifier of the authenticated user
     * @param name user's full name
     * @param email user's email address
     * @param role user's assigned role
     * @param status user's account status
     * @param message login result message
     * @param token JWT generated for the authenticated user
     */
    public LoginResponse(
            Long userId,
            String name,
            String email,
            UserRole role,
            UserStatus status,
            String message,
            String token) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.message = message;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the JWT generated after successful authentication.
     *
     * @return JWT authentication token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the JWT generated after successful authentication.
     *
     * @param token JWT authentication token
     */
    public void setToken(String token) {
        this.token = token;
    }
}
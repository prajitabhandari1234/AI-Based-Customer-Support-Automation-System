package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.LoginRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.LoginResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.RegisterRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import jakarta.validation.Valid;

/**
 * Handles user registration, login, and current-user requests.
 *
 * <p>
 * This controller provides authentication-related REST API endpoints
 * for registering new users, authenticating existing users, and
 * retrieving information about the currently authenticated user.
 * </p>
 *
 * <p>
 * Authentication routes are kept separate from the main user-management
 * endpoints to provide a clear separation between authentication
 * operations and administrative user-management functionality.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    /**
     * Creates the authentication controller with the required user service.
     *
     * @param userService service responsible for user registration,
     *                    authentication, and current-user operations
     */
    public AuthController(UserService userService) {

        this.userService = userService;

    }

    /**
     * Registers a new customer and immediately authenticates the
     * newly created account.
     *
     * <p>
     * The registration request is validated before the user is registered.
     * After successful registration, a login request is created using the
     * supplied email address and password. The new user is then authenticated
     * so that a login response can be returned without requiring a separate
     * login request.
     * </p>
     *
     * <p>
     * A successful registration returns HTTP 201 Created together with
     * the authentication response.
     * </p>
     *
     * @param request validated registration information containing the
     *                details required to create the user account
     * @return a response containing the login information for the
     *         newly registered user
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        userService.registerUser(request);

        LoginRequest loginRequest = new LoginRequest(
                request.getEmail(),
                request.getPassword());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.authenticateUser(loginRequest));

    }

    /**
     * Authenticates a user using the supplied login credentials.
     *
     * <p>
     * The login request is validated before being passed to the user
     * service for authentication.
     * </p>
     *
     * @param request validated login request containing the user's credentials
     * @return a response containing the authentication result
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.authenticateUser(request));

    }

    /**
     * Retrieves the currently authenticated user's information.
     *
     * <p>
     * The user service determines the current user from the application's
     * authentication context and returns the corresponding user information.
     * </p>
     *
     * @return a response containing the currently authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {

        return ResponseEntity.ok(userService.currentUser());

    }

}
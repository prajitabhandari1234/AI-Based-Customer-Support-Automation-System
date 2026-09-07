package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * REST controller responsible for authentication-related operations
 * within the AI-Based Customer Support Automation System.
 *
 * <p>
 * Provides public API endpoints for customer registration and
 * user authentication. JWT-based authentication will be integrated
 * separately as part of the security implementation.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    /**
     * Constructs a new {@code AuthController} with the required
     * user service.
     *
     * @param userService service used to manage user registration
     *                    and authentication
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new customer account.
     *
     * <p>
     * The registration request is validated before processing.
     * The backend automatically assigns the CUSTOMER role and ACTIVE
     * account status. The supplied password is securely hashed before
     * the user is stored in the database.
     * </p>
     *
     * @param request registration information supplied by the customer
     * @return the newly registered customer account
     */
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        User registeredUser = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredUser);
    }

    /**
     * Authenticates a user using their email address and password.
     *
     * <p>
     * The supplied credentials are validated before authentication.
     * If authentication succeeds, safe account information is returned.
     * Password information is never included in the response.
     * </p>
     *
     * @param request login credentials supplied by the user
     * @return information about the authenticated user
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.authenticateUser(request);

        return ResponseEntity.ok(response);
    }
}
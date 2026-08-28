package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.RegisterRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for authentication-related operations
 * within the AI-Based Customer Support Automation System.
 *
 * <p>This controller provides public endpoints for account registration
 * and will later support user authentication and JWT-based login.</p>
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
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new customer account.
     *
     * <p>The registration request is validated before processing.
     * The backend automatically assigns the CUSTOMER role and ACTIVE
     * account status. The supplied password is securely hashed before
     * the user is stored in the database.</p>
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
}
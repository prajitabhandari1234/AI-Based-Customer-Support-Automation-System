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
 * Handles user registration, login and current-user requests.
 * Keeps the authentication routes separate from the main user management endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Registers the customer and immediately returns a login response so they can continue without a second request.
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.authenticateUser(request));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(userService.currentUser());
    }
}

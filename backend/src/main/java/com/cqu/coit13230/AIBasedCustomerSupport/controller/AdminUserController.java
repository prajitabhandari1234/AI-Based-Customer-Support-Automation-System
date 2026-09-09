package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AdminUserCreateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.AdminUserUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.UpdateUserRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import jakarta.validation.Valid;

/**
 * Handles admin endpoints for creating and managing users.
 * These routes are used by admins to view, create and update system accounts.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/agents")
    public ResponseEntity<List<User>> getActiveAgents() {
        return ResponseEntity.ok(userService.getActiveAgents());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @Valid @RequestBody AdminUserCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUserByAdmin(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        return ResponseEntity.ok(userService.updateUserByAdmin(userId, request));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> patchUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(userService.patchUserByAdmin(userId, request));
    }
}

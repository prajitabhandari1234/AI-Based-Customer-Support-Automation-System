package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AdminUserUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for administrator user management.
 *
 * <p>
 * Provides administrative endpoints for retrieving users and
 * managing user roles and account statuses.
 * Access to these endpoints is restricted to administrators
 * through Spring Security.
 * </p>
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    /**
     * Constructs a new {@code AdminUserController}.
     *
     * @param userService service used to manage user accounts
     */
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all registered users.
     *
     * @return a list containing all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Retrieves a specific user by identifier.
     *
     * @param userId identifier of the user
     * @return the requested user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserById(userId));
    }

    /**
     * Updates the role and account status of a user.
     *
     * <p>
     * This endpoint allows an administrator to manage
     * user permissions and account availability.
     * </p>
     *
     * @param userId identifier of the user to update
     * @param request new role and account status
     * @return the updated user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        return ResponseEntity.ok(
                userService.updateUserByAdmin(userId, request));
    }
}
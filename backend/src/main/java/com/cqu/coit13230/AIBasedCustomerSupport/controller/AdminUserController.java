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
 * Handles administrator endpoints for creating and managing system users.
 *
 * <p>
 * This controller provides administrative operations for viewing,
 * creating, updating, and partially updating user accounts.
 * It also provides access to the list of active support agents.
 * </p>
 *
 * <p>
 * Access to these endpoints is controlled by the application's
 * Spring Security configuration and is intended for administrators.
 * </p>
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    /**
     * Creates the admin user controller with the required user service.
     *
     * @param userService service responsible for user-management operations
     */
    public AdminUserController(UserService userService) {

        this.userService = userService;

    }

    /**
     * Retrieves all users registered in the system.
     *
     * @return a response containing the list of all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(userService.getAllUsers());

    }

    /**
     * Retrieves all active support agents.
     *
     * @return a response containing the list of active support agents
     */
    @GetMapping("/agents")
    public ResponseEntity<List<User>> getActiveAgents() {

        return ResponseEntity.ok(userService.getActiveAgents());

    }

    /**
     * Retrieves a specific user using the supplied user identifier.
     *
     * @param userId unique identifier of the user to retrieve
     * @return a response containing the requested user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {

        return ResponseEntity.ok(userService.getUserById(userId));

    }

    /**
     * Creates a new user account through an administrator operation.
     *
     * <p>
     * The supplied request is validated before the user is created.
     * A successful operation returns HTTP 201 Created.
     * </p>
     *
     * @param request validated information required to create the user
     * @return a response containing the newly created user
     */
    @PostMapping
    public ResponseEntity<User> createUser(
            @Valid @RequestBody AdminUserCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUserByAdmin(request));

    }

    /**
     * Updates an existing user using the supplied administrator update request.
     *
     * <p>
     * The request is validated before the update is passed to the
     * user service.
     * </p>
     *
     * @param userId  unique identifier of the user to update
     * @param request validated user information to apply
     * @return a response containing the updated user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        return ResponseEntity.ok(userService.updateUserByAdmin(userId, request));

    }

    /**
     * Partially updates an existing user account.
     *
     * <p>
     * This operation allows selected user information to be modified
     * without requiring a complete replacement of the user data.
     * </p>
     *
     * @param userId  unique identifier of the user to update
     * @param request information containing the fields to be updated
     * @return a response containing the updated user
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<User> patchUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(userService.patchUserByAdmin(userId, request));

    }

}
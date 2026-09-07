package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import jakarta.validation.Valid;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link User} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting users through the {@link UserService}.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructs a new {@code UserController} with the required user service.
     *
     * @param userService service used to manage user operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all users.
     *
     * @return a list of all users
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Retrieves a user by identifier.
     *
     * @param userId the identifier of the user
     * @return the requested user
     * @throws ResourceNotFoundException if no user exists with the specified identifier
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Creates a new user.
     *
     * @param user the user to create
     * @return the created user
     */
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.saveUser(user);
    }

    /**
     * Updates an existing user.
     *
     * @param userId the identifier of the user to update
     * @param user the updated user information
     * @return the updated user
     * @throws ResourceNotFoundException if no user exists with the specified identifier
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody User user) {

        userService.getUserById(userId);

        user.setUserId(userId);

        return ResponseEntity.ok(userService.saveUser(user));
    }

    /**
     * Deletes a user by identifier.
     *
     * @param userId the identifier of the user to delete
     * @return HTTP 204 when the user is deleted successfully
     * @throws ResourceNotFoundException if no user exists with the specified identifier
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {

        userService.getUserById(userId);

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }
}
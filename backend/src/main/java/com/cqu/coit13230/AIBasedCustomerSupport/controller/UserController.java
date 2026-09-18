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

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import jakarta.validation.Valid;

/**
 * Provides administrator CRUD endpoints for users.
 *
 * <p>
 * This controller exposes the general user-management operations provided
 * by the application, including retrieving, creating, updating, and
 * deleting user records.
 * </p>
 *
 * <p>
 * User-related operations are delegated to the {@link UserService},
 * which manages the associated business logic and data operations.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates the user controller with the required user service.
     *
     * @param userService service responsible for managing user records
     */
    public UserController(UserService userService) {

        this.userService = userService;

    }

    /**
     * Retrieves all user records available in the system.
     *
     * @return a list containing all users
     */
    @GetMapping
    public List<User> getAllUsers() {

        return userService.getAllUsers();

    }

    /**
     * Retrieves a specific user using the user's unique identifier.
     *
     * @param userId unique identifier of the user to retrieve
     * @return a response containing the requested user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {

        return ResponseEntity.ok(userService.getUserById(userId));

    }

    /**
     * Creates and stores a new user.
     *
     * <p>
     * The supplied user information is validated before being passed
     * to the user service for persistence.
     * </p>
     *
     * @param user validated user information to create
     * @return the newly created and stored user
     */
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {

        return userService.saveUser(user);

    }

    /**
     * Updates an existing user identified by the user's unique identifier.
     *
     * <p>
     * The existing user is first retrieved to confirm that the record
     * exists. The supplied user is then assigned the same identifier
     * before being saved through the user service.
     * </p>
     *
     * @param userId unique identifier of the user to update
     * @param user   validated user information containing the updated values
     * @return a response containing the updated user
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
     * Deletes an existing user using the user's unique identifier.
     *
     * <p>
     * After the user is successfully deleted, the endpoint returns
     * an HTTP 204 No Content response.
     * </p>
     *
     * @param userId unique identifier of the user to delete
     * @return an empty response indicating successful deletion
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();

    }

}
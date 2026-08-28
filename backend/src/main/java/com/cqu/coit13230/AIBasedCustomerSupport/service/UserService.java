package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.RegisterRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.DuplicateResourceException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link User} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, deleting, and registering users through the
 * {@link UserRepository}.
 * </p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new {@code UserService} with the required dependencies.
     *
     * @param userRepository repository used to access user data
     * @param passwordEncoder encoder used to securely hash user passwords
     */
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new customer account.
     *
     * <p>
     * The email address is checked to ensure that it is not already
     * registered. The supplied password is securely hashed before the user
     * is persisted. New public registrations are automatically assigned
     * the {@link UserRole#CUSTOMER} role and {@link UserStatus#ACTIVE}
     * account status.
     * </p>
     *
     * @param request registration information supplied by the customer
     * @return the newly registered user
     * @throws IllegalArgumentException if the email address is already registered
     */
    public User registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "Email is already registered: " + email);
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    /**
     * Creates or updates a user.
     *
     * @param user the user to be saved
     * @return the saved user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves all users.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param userId the unique identifier of the user
     * @return the user associated with the specified identifier
     * @throws ResourceNotFoundException if no user exists with the specified identifier
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId));
    }

    /**
     * Deletes a user by identifier.
     *
     * @param userId the identifier of the user to delete
     * @throws ResourceNotFoundException if no user exists with the specified identifier
     */
    public void deleteUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with ID: " + userId);
        }

        userRepository.deleteById(userId);
    }
}
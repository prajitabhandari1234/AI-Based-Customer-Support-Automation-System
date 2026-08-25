package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link User} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting users through the {@link UserRepository}.
 * </p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructs a new {@code UserService} with the required
     * user repository dependency.
     *
     * @param userRepository repository used to access user data
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AdminUserUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.LoginRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.LoginResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.RegisterRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.AccountInactiveException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.AuthenticationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.DuplicateResourceException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.security.JwtService;

/**
 * Service class responsible for managing {@link User} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, deleting, registering, and authenticating users through
 * the {@link UserRepository}.
 * </p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Constructs a new {@code UserService} with the required dependencies.
     *
     * @param userRepository repository used to access user data
     * @param passwordEncoder encoder used to securely hash and verify passwords
     * @param jwtService service used to generate JWT authentication tokens
     */
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new customer account.
     *
     * <p>
     * The email address is normalised before being checked for duplicates.
     * The supplied password is securely hashed before the account is stored.
     * Public registrations are automatically assigned the
     * {@link UserRole#CUSTOMER} role and {@link UserStatus#ACTIVE} status.
     * </p>
     *
     * @param request registration information supplied by the customer
     * @return the newly registered user
     * @throws DuplicateResourceException if the email address is already registered
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
     * Authenticates a user using their email address and password.
     *
     * <p>
     * The supplied email address is normalised before lookup. The plain-text
     * password is compared with the stored BCrypt password hash using the
     * configured {@link PasswordEncoder}.
     * </p>
     *
     * <p>
     * Only accounts with {@link UserStatus#ACTIVE} status are permitted
     * to authenticate. After successful authentication, a signed JWT is
     * generated and returned to the client.
     * </p>
     *
     * @param request login credentials supplied by the user
     * @return safe account information together with a JWT
     * @throws AuthenticationException if the credentials are invalid
     * @throws AccountInactiveException if the account is inactive
     */
    public LoginResponse authenticateUser(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AuthenticationException(
                                "Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new AuthenticationException(
                    "Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountInactiveException(
                    "User account is inactive");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name());

        return new LoginResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                "Login successful",
                token);
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

    /**
     * Updates the role and account status of an existing user.
     *
     * <p>
     * This operation is intended for administrator user management.
     * Only administrative fields such as the user's role and account
     * status are modified. Other account information remains unchanged.
     * </p>
     *
     * @param userId the identifier of the user to update
     * @param request the new role and account status
     * @return the updated user
     * @throws ResourceNotFoundException if the user does not exist
     */
    public User updateUserByAdmin(
            Long userId,
            AdminUserUpdateRequest request) {

        User user = getUserById(userId);

        user.setRole(request.getRole());
        user.setStatus(request.getStatus());

        return userRepository.save(user);
    }
}
package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AdminUserCreateRequest;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AdminUserUpdateRequest;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.LoginRequest;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.LoginResponse;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.RegisterRequest;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.UpdateUserRequest;

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
 * Provides business logic for user registration, authentication,
 * retrieval, persistence, and administrative user management.
 *
 * <p>
 * This service manages customer self-registration, password verification,
 * JWT-based login responses, authenticated user retrieval, and
 * administrator-controlled account creation and updates.
 * </p>
 *
 * <p>
 * Important authentication and registration activities are also recorded
 * through the system logging service.
 * </p>
 */
@Service

public class UserService {

    /**
     * Repository used to persist and retrieve user accounts.
     */
    private final UserRepository userRepository;

    /**
     * Password encoder used to securely hash and verify user passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Service used to generate JWT authentication tokens.
     */
    private final JwtService jwtService;

    /**
     * Service used to record authentication and user-related system events.
     */
    private final SystemLogService systemLogService;

    /**
     * Creates a user service with the dependencies required for user
     * persistence, password processing, JWT generation, and system logging.
     *
     * @param userRepository   repository used to access user records
     * @param passwordEncoder  encoder used to hash and verify passwords
     * @param jwtService       service used to generate JWT authentication tokens
     * @param systemLogService service used to record system events
     */
    public UserService(

            UserRepository userRepository,

            PasswordEncoder passwordEncoder,

            JwtService jwtService,

            SystemLogService systemLogService) {

        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;

        this.jwtService = jwtService;

        this.systemLogService = systemLogService;

    }

    /**
     * Registers a new self-service user account.
     *
     * <p>
     * The supplied email address is normalized and checked for an existing
     * account before registration. The password is encoded before storage,
     * and newly registered users are assigned the
     * {@link UserRole#CLIENT} role and {@link UserStatus#ACTIVE} status.
     * The successful registration is also recorded in the system log.
     * </p>
     *
     * @param request registration information supplied by the user
     * @return newly created and persisted user
     * @throws DuplicateResourceException if the supplied email address
     *                                    is already registered
     */
    public User registerUser(RegisterRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new DuplicateResourceException("Email is already registered: " + email);

        }

        User user = new User();

        user.setName(request.getName().trim());

        user.setEmail(email);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setRole(UserRole.CLIENT);

        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        systemLogService.logEvent("USER_REGISTERED", "Client account registered", savedUser, null);

        return savedUser;

    }

    /**
     * Authenticates a user using the supplied login credentials.
     *
     * <p>
     * The email address is normalized before the corresponding account is
     * retrieved. The supplied password is compared with the stored encoded
     * password, and the account must have {@link UserStatus#ACTIVE} status
     * before authentication succeeds.
     * </p>
     *
     * <p>
     * Failed authentication attempts are recorded in the system log.
     * Following successful authentication, a JWT is generated and returned
     * as part of the login response.
     * </p>
     *
     * @param request login credentials supplied by the user
     * @return successful login response containing the authenticated user
     *         information and generated JWT
     * @throws AuthenticationException  if the email or password is invalid
     * @throws AccountInactiveException if the user account is inactive
     */
    public LoginResponse authenticateUser(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

            systemLogService.logLoginFailure(email, user);

            throw new AuthenticationException("Invalid email or password");

        }

        if (user.getStatus() != UserStatus.ACTIVE) {

            systemLogService.logLoginFailure(email, user);

            throw new AccountInactiveException("User account is inactive");

        }

        String token = jwtService.generateToken(user);

        systemLogService.logLoginSuccess(user);

        return LoginResponse.from(user, token, "Login successful");

    }

    /**
     * Retrieves the complete user record for the currently authenticated user.
     *
     * <p>
     * The authenticated email address is obtained from the Spring Security
     * context and used to retrieve the corresponding user from the database.
     * </p>
     *
     * @return currently authenticated user
     * @throws AuthenticationException   if authentication information is
     *                                   unavailable from the security context
     * @throws ResourceNotFoundException if no stored user corresponds to
     *                                   the authenticated identity
     */
    public User currentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {

            throw new AuthenticationException("Authenticated user was not found");

        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())

                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));

    }

    /**
     * Normalizes the user's email address and persists the supplied user.
     *
     * @param user user to persist
     * @return persisted user
     */
    public User saveUser(User user) {

        user.setEmail(normalizeEmail(user.getEmail()));

        return userRepository.save(user);

    }

    /**
     * Retrieves all user accounts stored in the database.
     *
     * @return list containing all users
     */
    public List<User> getAllUsers() {

        return userRepository.findAll();

    }

    /**
     * Retrieves a user using the supplied unique identifier.
     *
     * @param userId unique identifier of the user
     * @return user matching the supplied identifier
     * @throws ResourceNotFoundException if no user exists with the
     *                                   supplied identifier
     */
    public User getUserById(Long userId) {

        return userRepository.findById(userId)

                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    }

    /**
     * Deletes a user account using its unique identifier.
     *
     * <p>
     * The user is retrieved first to verify that the account exists before
     * the repository deletion operation is performed.
     * </p>
     *
     * @param userId unique identifier of the user to delete
     * @throws ResourceNotFoundException if no user exists with the
     *                                   supplied identifier
     */
    public void deleteUser(Long userId) {

        getUserById(userId);

        userRepository.deleteById(userId);

    }

    /**
     * Creates a user account using administrator-supplied account details.
     *
     * <p>
     * The email address is normalized and checked for duplicates before
     * account creation. The supplied password is encoded before storage,
     * while the requested role and account status are assigned directly
     * to the new user.
     * </p>
     *
     * @param request administrator request containing the new user's details
     * @return newly created and persisted user
     * @throws DuplicateResourceException if the supplied email address
     *                                    is already registered
     */
    public User createUserByAdmin(AdminUserCreateRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new DuplicateResourceException("Email is already registered: " + email);

        }

        User user = new User();

        user.setName(request.getName().trim());

        user.setEmail(email);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setRole(request.getRole());

        user.setStatus(request.getStatus());

        return userRepository.save(user);

    }

    /**
     * Updates the role and account status of an existing user.
     *
     * @param userId  unique identifier of the user to update
     * @param request request containing the new role and account status
     * @return updated and persisted user
     * @throws ResourceNotFoundException if no user exists with the
     *                                   supplied identifier
     */
    public User updateUserByAdmin(Long userId, AdminUserUpdateRequest request) {

        User user = getUserById(userId);

        user.setRole(request.getRole());

        user.setStatus(request.getStatus());

        return userRepository.save(user);

    }

    /**
     * Applies administrator-supplied partial updates to an existing user.
     *
     * <p>
     * The user's name, role, and account status are updated only when
     * corresponding non-null values are supplied. A supplied name must also
     * contain non-whitespace content before it replaces the existing name.
     * </p>
     *
     * @param userId  unique identifier of the user to update
     * @param request request containing optional user values to update
     * @return updated and persisted user
     * @throws ResourceNotFoundException if no user exists with the
     *                                   supplied identifier
     */
    public User patchUserByAdmin(Long userId, UpdateUserRequest request) {

        User user = getUserById(userId);

        if (request.getName() != null && !request.getName().isBlank()) {

            user.setName(request.getName().trim());

        }

        if (request.getRole() != null) {

            user.setRole(request.getRole());

        }

        if (request.getStatus() != null) {

            user.setStatus(request.getStatus());

        }

        return userRepository.save(user);

    }

    /**
     * Retrieves all users who currently have the support-agent role
     * and active account status.
     *
     * @return list of active support agents ordered according to the
     *         repository query
     */
    public List<User> getActiveAgents() {

        return userRepository.findByRoleAndStatusOrderByNameAsc(

                UserRole.AGENT,

                UserStatus.ACTIVE);

    }

    /**
     * Normalizes an email address by removing surrounding whitespace
     * and converting the value to lowercase.
     *
     * @param email email address to normalize
     * @return normalized email address
     */
    private String normalizeEmail(String email) {

        return email.trim().toLowerCase();

    }

}
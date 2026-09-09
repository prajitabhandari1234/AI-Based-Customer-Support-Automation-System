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
 * Handles registration, login and user management operations.
 * It also handles password checks, JWT login responses and admin account changes.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SystemLogService systemLogService;

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

    // Creates self-registered accounts with the customer role and active status by default.
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

    /*
     * Verifies the stored password and confirms the account is active before login succeeds.
     * A JWT is only created after both checks pass.
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

    // Uses the email stored in Spring Security to load the full current user record.
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new AuthenticationException("Authenticated user was not found");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    public User saveUser(User user) {
        user.setEmail(normalizeEmail(user.getEmail()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    public void deleteUser(Long userId) {
        getUserById(userId);
        userRepository.deleteById(userId);
    }

    // Lets admins create staff or customer accounts while still checking for duplicate email addresses.
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

    public User updateUserByAdmin(Long userId, AdminUserUpdateRequest request) {
        User user = getUserById(userId);
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        return userRepository.save(user);
    }

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

    public List<User> getActiveAgents() {
        return userRepository.findByRoleAndStatusOrderByNameAsc(
                UserRole.AGENT,
                UserStatus.ACTIVE);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

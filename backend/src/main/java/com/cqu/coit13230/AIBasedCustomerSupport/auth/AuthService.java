package com.cqu.coit13230.AIBasedCustomerSupport.auth;

import com.cqu.coit13230.AIBasedCustomerSupport.common.BadRequestException;
import com.cqu.coit13230.AIBasedCustomerSupport.log.SystemLogService;
import com.cqu.coit13230.AIBasedCustomerSupport.security.JwtService;
import com.cqu.coit13230.AIBasedCustomerSupport.user.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SystemLogService logs;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       SystemLogService logs) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.logs = logs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CLIENT);
        user.setStatus(UserStatus.ACTIVE);
        user = users.save(user);
        logs.record("USER_REGISTERED", "Client account registered", user, null);
        return new AuthResponse(jwtService.generateToken(user), UserView.from(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );
        User user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Account not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("This account is inactive");
        }
        logs.record("LOGIN_SUCCESS", "Successful login", user, null);
        return new AuthResponse(jwtService.generateToken(user), UserView.from(user));
    }
}

package com.cqu.coit13230.AIBasedCustomerSupport.auth;

import com.cqu.coit13230.AIBasedCustomerSupport.user.UserService;
import com.cqu.coit13230.AIBasedCustomerSupport.user.UserView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserView me() {
        return UserView.from(userService.currentUser());
    }
}

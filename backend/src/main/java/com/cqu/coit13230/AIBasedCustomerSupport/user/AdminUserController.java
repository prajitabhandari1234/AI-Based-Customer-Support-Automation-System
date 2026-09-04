package com.cqu.coit13230.AIBasedCustomerSupport.user;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserView> allUsers() {
        return userService.listAll();
    }

    @GetMapping("/agents")
    public List<UserView> agents() {
        return userService.activeAgents();
    }

    @PatchMapping("/{id}")
    public UserView update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }
}

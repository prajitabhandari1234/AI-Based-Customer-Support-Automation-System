package com.cqu.coit13230.AIBasedCustomerSupport.user;

import com.cqu.coit13230.AIBasedCustomerSupport.common.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new NotFoundException("Authenticated user was not found");
        }
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Authenticated user was not found"));
    }

    @Transactional(readOnly = true)
    public User require(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserView> listAll() {
        return users.findAllByOrderByCreatedAtDesc().stream().map(UserView::from).toList();
    }

    @Transactional
    public UserView update(Long id, UpdateUserRequest request) {
        User user = require(id);
        if (request.name() != null && !request.name().isBlank()) user.setName(request.name().trim());
        if (request.role() != null) user.setRole(request.role());
        if (request.status() != null) user.setStatus(request.status());
        return UserView.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserView> activeAgents() {
        return users.findByRoleAndStatusOrderByNameAsc(Role.AGENT, UserStatus.ACTIVE)
                .stream().map(UserView::from).toList();
    }
}

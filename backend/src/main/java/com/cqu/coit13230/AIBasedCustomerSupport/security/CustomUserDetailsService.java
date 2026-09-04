package com.cqu.coit13230.AIBasedCustomerSupport.security;

import com.cqu.coit13230.AIBasedCustomerSupport.user.User;
import com.cqu.coit13230.AIBasedCustomerSupport.user.UserRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.user.UserStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }
}

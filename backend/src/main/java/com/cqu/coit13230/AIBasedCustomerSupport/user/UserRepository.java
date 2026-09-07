package com.cqu.coit13230.AIBasedCustomerSupport.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findFirstByRoleAndStatusOrderByIdAsc(Role role, UserStatus status);
    List<User> findAllByOrderByCreatedAtDesc();
    List<User> findByRoleAndStatusOrderByNameAsc(Role role, UserStatus status);
}

package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

/**
 * Provides database access and user lookup queries.
 * Spring Data JPA provides the standard CRUD operations while this interface adds project-specific lookups.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleAndStatusOrderByNameAsc(UserRole role, UserStatus status);

    Optional<User> findFirstByRoleAndStatusOrderByUserIdAsc(UserRole role, UserStatus status);
}

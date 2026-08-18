package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;

/**
 * Repository interface for managing {@link User} entities.
 *
 * <p>
 * Provides standard CRUD operations and database access for user records
 * through Spring Data JPA.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
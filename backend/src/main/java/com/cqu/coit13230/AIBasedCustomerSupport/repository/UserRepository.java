package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.Optional;

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
 *
 * <p>
 * Additional query methods are provided for locating users by email
 * address and checking whether an email address is already registered.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by their email address.
     *
     * <p>This method is primarily used during authentication to locate
     * the account associated with a supplied email address.</p>
     *
     * @param email the email address of the user
     * @return an optional containing the matching user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Determines whether a user account already exists with the
     * specified email address.
     *
     * <p>This method is used during registration to prevent multiple
     * accounts from being created with the same email address.</p>
     *
     * @param email the email address to check
     * @return {@code true} if the email is already registered;
     *         {@code false} otherwise
     */
    boolean existsByEmail(String email);
}
package com.cqu.coit13230.AIBasedCustomerSupport.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

/**
 * Provides database access and query operations for {@link User}
 * entities within the AI-based customer support system.
 *
 * <p>
 * This repository extends Spring Data JPA's {@link JpaRepository},
 * which provides standard persistence operations such as creating,
 * retrieving, updating, and deleting user records.
 * </p>
 *
 * <p>
 * It also defines project-specific query methods for locating users
 * by email address, checking whether an email address already exists,
 * and retrieving users according to their role and account status.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address without considering differences
     * in letter case.
     *
     * <p>
     * This allows email addresses to be matched consistently regardless
     * of whether uppercase or lowercase characters are supplied.
     * </p>
     *
     * @param email email address used to locate the user
     * @return an {@link Optional} containing the matching user if found,
     *         or an empty {@code Optional} if no matching user exists
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Determines whether a user account exists with the specified
     * email address without considering differences in letter case.
     *
     * @param email email address to check
     * @return {@code true} if a user with the specified email address
     *         exists; otherwise {@code false}
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Retrieves users matching the specified role and account status,
     * ordered by name in ascending order.
     *
     * <p>
     * This query can be used to retrieve groups of users such as
     * active support agents while maintaining alphabetical ordering
     * by user name.
     * </p>
     *
     * @param role   role used to filter users
     * @param status account status used to filter users
     * @return a list of users matching the specified role and status,
     *         ordered by name in ascending order
     */
    List<User> findByRoleAndStatusOrderByNameAsc(UserRole role, UserStatus status);

    /**
     * Finds the first user matching the specified role and account
     * status, ordered by user identifier in ascending order.
     *
     * <p>
     * When multiple users satisfy the supplied criteria, the user
     * with the lowest identifier is returned.
     * </p>
     *
     * @param role   role used to filter users
     * @param status account status used to filter users
     * @return an {@link Optional} containing the first matching user
     *         if one exists, or an empty {@code Optional} otherwise
     */
    Optional<User> findFirstByRoleAndStatusOrderByUserIdAsc(UserRole role, UserStatus status);

}
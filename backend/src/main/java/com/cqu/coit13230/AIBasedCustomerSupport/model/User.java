package com.cqu.coit13230.AIBasedCustomerSupport.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user within the AI-Based Customer Support system.
 * A user may have the role of customer, support agent, or administrator.
 *
 * <p>This entity stores user account information including authentication
 * details, role, account status, and account creation timestamp.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    /**
     * Full name of the user.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Email address used to uniquely identify the user's account.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Securely hashed password used for user authentication.
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Role assigned to the user, which determines their system permissions.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * Current status of the user's account.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    /**
     * Date and time when the user account was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Sets the account creation timestamp before the entity is first
     * persisted to the database.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
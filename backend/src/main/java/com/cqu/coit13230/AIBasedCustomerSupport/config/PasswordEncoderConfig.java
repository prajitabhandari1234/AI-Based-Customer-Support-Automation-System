package com.cqu.coit13230.AIBasedCustomerSupport.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class responsible for providing password encoding
 * functionality for the application.
 *
 * <p>The application uses BCrypt to securely hash user passwords before
 * they are stored in the database. Plain-text passwords must never be
 * persisted directly.</p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates the password encoder used for hashing and verifying
     * user passwords.
     *
     * @return a BCrypt-based password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
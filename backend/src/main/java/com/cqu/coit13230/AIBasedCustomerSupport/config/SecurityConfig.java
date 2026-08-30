package com.cqu.coit13230.AIBasedCustomerSupport.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cqu.coit13230.AIBasedCustomerSupport.security.JwtAuthenticationFilter;

/**
 * Security configuration for JWT authentication and role-based
 * access control within the application.
 *
 * <p>
 * Public authentication endpoints are accessible without a JWT.
 * Protected endpoints require authentication and may additionally
 * restrict access according to the authenticated user's role.
 * </p>
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs the security configuration.
     *
     * @param jwtAuthenticationFilter filter used to validate JWT tokens
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures JWT authentication and role-based endpoint access.
     *
     * @param http Spring Security HTTP configuration
     * @return configured security filter chain
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login")
                        .permitAll()

                        // User management is restricted to administrators.
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // Administrator-specific endpoints.
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Support-agent endpoints.
                        .requestMatchers("/api/agent/**")
                        .hasAnyRole("SUPPORT_AGENT", "ADMIN")

                        // Customer-specific endpoints.
                        .requestMatchers("/api/customer/**")
                        .hasRole("CUSTOMER")

                        // Remaining API endpoints require authentication.
                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest()
                        .permitAll())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
package com.cqu.coit13230.AIBasedCustomerSupport.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cqu.coit13230.AIBasedCustomerSupport.security.JwtAuthenticationFilter;

/**
 * Security configuration for JWT authentication, role-based access control,
 * and Cross-Origin Resource Sharing (CORS) within the application.
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
     * Configures JWT authentication, CORS, and role-based endpoint access.
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

                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints are publicly accessible.
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

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for frontend
     * applications communicating with the backend REST API.
     *
     * <p>
     * During development, requests from the local React development
     * server are permitted. The allowed origin should be updated when
     * the frontend is deployed to a production environment.
     * </p>
     *
     * @return configured CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"));

        configuration.setExposedHeaders(List.of(
                "Authorization"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration);

        return source;
    }
}
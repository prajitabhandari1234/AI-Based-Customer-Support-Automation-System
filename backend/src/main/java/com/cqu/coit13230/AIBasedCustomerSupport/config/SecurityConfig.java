package com.cqu.coit13230.AIBasedCustomerSupport.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cqu.coit13230.AIBasedCustomerSupport.security.JwtAuthenticationFilter;

/**
 * Configures security for the AI-Based Customer Support Automation System.
 *
 * <p>
 * This configuration defines authentication and authorization rules for
 * application endpoints, integrates JWT-based authentication, configures
 * stateless session management, enables CORS support, and provides password
 * encoding using BCrypt.
 * </p>
 *
 * <p>
 * Access to protected resources is controlled according to the CLIENT,
 * AGENT, and ADMIN roles, while selected authentication, actuator, and
 * H2 console endpoints are publicly accessible.
 * </p>
 */
@Configuration
public class SecurityConfig {

        /**
         * JWT authentication filter used to validate authentication tokens
         * before requests reach Spring Security's username and password filter.
         */
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        /**
         * Creates the security configuration with the required JWT
         * authentication filter.
         *
         * @param jwtAuthenticationFilter filter used to authenticate requests
         *                                containing JWT tokens
         */
        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        /**
         * Configures the application's Spring Security filter chain.
         *
         * <p>
         * CSRF protection is disabled because the application uses stateless
         * JWT-based authentication. CORS support is enabled using the supplied
         * configuration source, and HTTP sessions are configured to remain
         * stateless.
         * </p>
         *
         * <p>
         * The authorization rules define public endpoints and restrict protected
         * API routes according to CLIENT, AGENT, and ADMIN roles. The JWT
         * authentication filter is executed before Spring Security's standard
         * username and password authentication filter.
         * </p>
         *
         * @param http                    Spring Security HTTP configuration
         * @param corsConfigurationSource source containing the application's
         *                                CORS configuration
         * @return configured Spring Security filter chain
         * @throws Exception if the security filter chain cannot be configured
         */
        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        CorsConfigurationSource corsConfigurationSource) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/register",
                                                                "/actuator/health",
                                                                "/actuator/info",
                                                                "/h2-console/**")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**", "/api/analytics/**", "/api/users/**",
                                                                "/api/system-logs/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/agent/**")
                                                .hasAnyRole("AGENT", "ADMIN")
                                                .requestMatchers("/api/customer/**", "/api/chat/**")
                                                .hasRole("CLIENT")
                                                .requestMatchers(HttpMethod.GET, "/api/knowledge-base/**",
                                                                "/api/knowledge/**")
                                                .authenticated()
                                                .requestMatchers("/api/knowledge-base/**", "/api/knowledge/**")
                                                .hasAnyRole("AGENT", "ADMIN")
                                                .requestMatchers("/api/conversations/**", "/api/messages/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/notifications")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.PATCH, "/api/notifications/*/read")
                                                .authenticated()
                                                .requestMatchers("/api/notifications/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/tickets/my",
                                                                "/api/tickets/my/summary")
                                                .hasRole("CLIENT")
                                                .requestMatchers(HttpMethod.POST, "/api/tickets/*/messages")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/tickets/*")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/tickets")
                                                .hasAnyRole("CLIENT", "ADMIN")
                                                .requestMatchers("/api/tickets/**")
                                                .hasRole("ADMIN")
                                                .anyRequest()
                                                .authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Provides the password encoder used by the application.
         *
         * <p>
         * BCrypt is used to securely hash user passwords before they are
         * persisted and to verify passwords during authentication.
         * </p>
         *
         * @return BCrypt-based password encoder
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Creates the CORS configuration used by the application's API endpoints.
         *
         * <p>
         * Allowed frontend origins are obtained from the
         * {@code app.cors.allowed-origins} configuration property. Multiple
         * origins can be supplied as a comma-separated list. Each configured
         * origin is trimmed and blank values are excluded.
         * </p>
         *
         * <p>
         * The configuration permits GET, POST, PUT, PATCH, DELETE, and OPTIONS
         * requests and allows Authorization and Content-Type request headers.
         * Credentials are also permitted for configured origins.
         * </p>
         *
         * @param allowedOrigins comma-separated list of frontend origins allowed
         *                       to access the application
         * @return CORS configuration source registered for all application paths
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.cors.allowed-origins}") String allowedOrigins) {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                Arrays.stream(allowedOrigins.split(","))
                                                .map(String::trim)
                                                .filter(value -> !value.isBlank())
                                                .toList());

                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                configuration.setExposedHeaders(List.of("Authorization"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
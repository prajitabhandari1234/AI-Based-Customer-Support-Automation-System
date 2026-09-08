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
 * Configures endpoint security, CORS and password encoding.
 * The rules define which routes are public and which require customer, agent or admin access.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /*
     * Defines the endpoint access rules used by Spring Security.
     * Public routes are permitted first, followed by role-specific customer, agent and admin routes.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/actuator/health",
                                "/actuator/info",
                                "/h2-console/**")
                        .permitAll()
                        .requestMatchers("/api/admin/**", "/api/analytics/**", "/api/users/**", "/api/system-logs/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/agent/**")
                        .hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers("/api/customer/**", "/api/chat/**")
                        .hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/knowledge-base/**", "/api/knowledge/**")
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
                        .requestMatchers(HttpMethod.GET, "/api/tickets/my", "/api/tickets/my/summary")
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Builds the CORS rules from the configured frontend origin list instead of hard-coding one client URL.
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

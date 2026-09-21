package com.cqu.coit13230.AIBasedCustomerSupport.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Performs JWT-based authentication for incoming HTTP requests within
 * the AI-based customer support system.
 *
 * <p>
 * This filter executes once per request and examines the
 * {@code Authorization} header for a bearer token. When a valid JWT is
 * provided, the corresponding active user is loaded from the database
 * and authenticated within the Spring Security context.
 * </p>
 *
 * <p>
 * Public authentication endpoints for user login and registration are
 * excluded from JWT filtering because these requests do not require an
 * existing authenticated session.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Prefix expected before a JWT value in the HTTP
     * {@code Authorization} header.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Service responsible for extracting information from JWTs and
     * validating their authenticity.
     */
    private final JwtService jwtService;

    /**
     * Repository used to retrieve the user associated with the email
     * address contained in a JWT.
     */
    private final UserRepository userRepository;

    /**
     * Creates a JWT authentication filter with the services required
     * to validate tokens and retrieve users.
     *
     * @param jwtService     service used to process and validate JWTs
     * @param userRepository repository used to retrieve user accounts
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Determines whether JWT filtering should be skipped for the
     * current request.
     *
     * <p>
     * Login and registration endpoints are public authentication
     * routes and therefore do not require an existing bearer token.
     * </p>
     *
     * @param request current HTTP request
     * @return {@code true} when the request targets the login or
     *         registration endpoint; otherwise {@code false}
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register");
    }

    /**
     * Processes the JWT authentication information associated with an
     * incoming HTTP request.
     *
     * <p>
     * The method reads the bearer token from the {@code Authorization}
     * header, extracts the user's email address, retrieves the matching
     * user account, verifies that the account is active, and validates
     * the token. When validation succeeds, a Spring Security
     * authentication object containing the user's role authority is
     * added to the {@link SecurityContextHolder}.
     * </p>
     *
     * <p>
     * If the token cannot be processed or validated, the security
     * context is cleared and the request continues through the filter
     * chain without authenticated user information.
     * </p>
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain filter chain used to continue request processing
     * @throws ServletException if a servlet-related error occurs while
     *                          processing the request
     * @throws IOException      if an input or output error occurs while
     *                          processing the request
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        try {
            String email = jwtService.extractEmail(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

                if (user != null
                        && user.getStatus() == UserStatus.ACTIVE
                        && jwtService.isTokenValid(token, user.getEmail())) {

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            List.of(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

}
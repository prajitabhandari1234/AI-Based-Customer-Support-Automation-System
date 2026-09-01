package com.cqu.coit13230.AIBasedCustomerSupport.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
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
 * Security filter responsible for validating JWT authentication tokens
 * included in incoming HTTP requests.
 *
 * <p>
 * The filter reads the Authorization header using the Bearer scheme.
 * When a valid JWT is supplied, the authenticated user's identity and
 * role are stored in the Spring Security context.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Constructs the JWT authentication filter.
     *
     * @param jwtService service used to process JWT tokens
     * @param userRepository repository used to retrieve users
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Determines whether JWT filtering should be skipped for a request.
     *
     * <p>
     * Public authentication endpoints do not require an existing JWT because
     * users must be able to register or obtain a new token by logging in.
     * </p>
     *
     * @param request incoming HTTP request
     * @return {@code true} when JWT filtering should be skipped
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register");
    }

    /**
     * Processes JWT authentication for incoming requests.
     *
     * @param request incoming HTTP request
     * @param response outgoing HTTP response
     * @param filterChain remaining security filter chain
     * @throws ServletException if servlet processing fails
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader
                .substring(BEARER_PREFIX.length())
                .trim();

        try {

            String email = jwtService.extractEmail(token);

            if (email != null
                    && SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                User user = userRepository.findByEmail(email)
                        .orElse(null);

                if (user == null) {
                    sendUnauthorized(
                            response,
                            "User associated with token was not found");
                    return;
                }

                if (user.getStatus() != UserStatus.ACTIVE) {
                    sendUnauthorized(
                            response,
                            "User account is inactive");
                    return;
                }

                if (!jwtService.isTokenValid(
                        token,
                        user.getEmail())) {

                    sendUnauthorized(
                            response,
                            "Invalid or expired token");
                    return;
                }

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                List.of(authority));

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception ex) {

            SecurityContextHolder.clearContext();

            sendUnauthorized(
                    response,
                    "Invalid or expired token");

            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Sends a structured HTTP 401 response.
     *
     * @param response HTTP response
     * @param message authentication failure message
     * @throws IOException if the response cannot be written
     */
    private void sendUnauthorized(
            HttpServletResponse response,
            String message) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        response.getWriter().write(
                "{\"status\":401,"
                        + "\"error\":\"Unauthorized\","
                        + "\"message\":\""
                        + message
                        + "\"}");
    }
}
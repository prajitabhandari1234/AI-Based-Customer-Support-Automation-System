package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns the logged-in user details together with the JWT token.
 *
 * <p>
 * This Data Transfer Object (DTO) represents the response returned
 * after a user has been successfully authenticated by the customer
 * support system.
 * </p>
 *
 * <p>
 * The response contains the authenticated user's details, account
 * role and status, authentication message, JWT token, and the
 * associated user object. The DTO keeps API response data separate
 * from the database entities.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Unique identifier of the authenticated user.
     */
    private Long userId;

    /**
     * Name of the authenticated user.
     */
    private String name;

    /**
     * Email address associated with the authenticated user's account.
     */
    private String email;

    /**
     * Role assigned to the authenticated user.
     *
     * <p>
     * The role represents the user's access level within the
     * customer support system.
     * </p>
     */
    private UserRole role;

    /**
     * Current account status of the authenticated user.
     */
    private UserStatus status;

    /**
     * Message associated with the authentication response.
     */
    private String message;

    /**
     * JWT token generated for the authenticated user.
     *
     * <p>
     * The token can be used to authenticate subsequent requests
     * to protected application endpoints.
     * </p>
     */
    private String token;

    /**
     * User object representing the authenticated user.
     */
    private User user;

    /**
     * Builds a login response from the authenticated user.
     *
     * <p>
     * This factory method obtains the required user information from
     * the supplied {@link User} object and combines it with the JWT
     * token and authentication message to create a complete
     * {@code LoginResponse}.
     * </p>
     *
     * @param user    authenticated user whose information is included
     *                in the login response
     * @param token   JWT token generated for the authenticated user
     * @param message message associated with the authentication result
     * @return a new login response containing the authenticated user's
     *         details, token, and message
     */
    public static LoginResponse from(User user, String token, String message) {

        return new LoginResponse(

                user.getUserId(),

                user.getName(),

                user.getEmail(),

                user.getRole(),

                user.getStatus(),

                message,

                token,

                user);

    }

}
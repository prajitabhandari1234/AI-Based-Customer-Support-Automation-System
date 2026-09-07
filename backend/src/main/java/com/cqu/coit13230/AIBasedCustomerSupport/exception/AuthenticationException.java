package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Exception thrown when user authentication fails.
 *
 * <p>This exception is used for invalid login credentials or when
 * a user account is not permitted to authenticate.</p>
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Constructs a new authentication exception.
     *
     * @param message description of the authentication failure
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when login or authentication fails.
 *
 * <p>
 * This custom runtime exception is thrown when a user cannot be
 * successfully authenticated, such as when the supplied login
 * credentials are invalid.
 * </p>
 *
 * <p>
 * The global exception handler can catch this exception and convert
 * it into a suitable HTTP error response for the client.
 * </p>
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Creates a new {@code AuthenticationException} with the specified
     * error message.
     *
     * @param message description of the authentication failure
     */
    public AuthenticationException(String message) {

        super(message);

    }

}
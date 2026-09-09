package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when login or authentication fails.
 * The global exception handler converts it into a suitable HTTP error response.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}

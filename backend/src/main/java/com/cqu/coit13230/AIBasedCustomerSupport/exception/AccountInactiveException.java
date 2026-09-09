package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when a user account is not active.
 * The global exception handler converts it into a suitable HTTP error response.
 */
public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException(String message) {
        super(message);
    }
}

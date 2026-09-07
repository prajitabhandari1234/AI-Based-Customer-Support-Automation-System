package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Exception thrown when an inactive user attempts to access
 * functionality that requires an active account.
 */
public class AccountInactiveException extends RuntimeException {

    /**
     * Constructs a new account inactive exception.
     *
     * @param message description of the account restriction
     */
    public AccountInactiveException(String message) {
        super(message);
    }
}
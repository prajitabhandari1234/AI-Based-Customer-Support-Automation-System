package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when a user account is not active.
 *
 * <p>
 * This custom runtime exception is thrown when an operation requires
 * an active user account but the associated account is currently
 * inactive.
 * </p>
 *
 * <p>
 * The global exception handler can catch this exception and convert
 * it into a suitable HTTP error response for the client.
 * </p>
 */
public class AccountInactiveException extends RuntimeException {

    /**
     * Creates a new {@code AccountInactiveException} with the specified
     * error message.
     *
     * @param message description of why the user account is considered
     *                inactive for the requested operation
     */
    public AccountInactiveException(String message) {

        super(message);

    }

}
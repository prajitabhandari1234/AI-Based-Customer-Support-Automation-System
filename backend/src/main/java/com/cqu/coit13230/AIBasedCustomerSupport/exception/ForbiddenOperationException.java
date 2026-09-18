package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when the current user cannot perform an operation.
 *
 * <p>
 * This custom runtime exception is thrown when an authenticated user
 * attempts to perform an operation that they are not permitted to
 * perform within the customer support system.
 * </p>
 *
 * <p>
 * The global exception handler can catch this exception and convert
 * it into a suitable HTTP error response for the client.
 * </p>
 */
public class ForbiddenOperationException extends RuntimeException {

    /**
     * Creates a new {@code ForbiddenOperationException} with the specified
     * error message.
     *
     * @param message description of why the requested operation is forbidden
     */
    public ForbiddenOperationException(String message) {

        super(message);

    }

}
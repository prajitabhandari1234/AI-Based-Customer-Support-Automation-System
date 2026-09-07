package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Exception thrown when an authenticated user attempts to perform
 * an operation that they are not authorised to perform.
 *
 * <p>
 * This exception is used when the user is successfully authenticated
 * but does not have permission to access or modify the requested
 * resource.
 * </p>
 */
public class ForbiddenOperationException extends RuntimeException {

    /**
     * Constructs a new {@code ForbiddenOperationException}
     * with the specified error message.
     *
     * @param message description of why the operation is forbidden
     */
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
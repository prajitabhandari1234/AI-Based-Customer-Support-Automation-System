package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when the current user cannot perform an operation.
 * The global exception handler converts it into a suitable HTTP error response.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}

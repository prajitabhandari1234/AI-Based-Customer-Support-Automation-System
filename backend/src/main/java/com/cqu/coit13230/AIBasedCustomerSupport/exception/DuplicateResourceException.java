package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when a record already exists.
 * The global exception handler converts it into a suitable HTTP error response.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Exception thrown when an attempt is made to create a resource
 * that already exists in the system.
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs a new duplicate resource exception.
     *
     * @param message description of the duplicate resource error
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
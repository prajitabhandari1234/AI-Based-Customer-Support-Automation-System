package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when a record already exists.
 *
 * <p>
 * This custom runtime exception is thrown when an attempt is made
 * to create or store a resource that already exists within the
 * application.
 * </p>
 *
 * <p>
 * The global exception handler can catch this exception and convert
 * it into a suitable HTTP error response for the client.
 * </p>
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Creates a new {@code DuplicateResourceException} with the specified
     * error message.
     *
     * @param message description of the duplicate resource or record
     */
    public DuplicateResourceException(String message) {

        super(message);

    }

}
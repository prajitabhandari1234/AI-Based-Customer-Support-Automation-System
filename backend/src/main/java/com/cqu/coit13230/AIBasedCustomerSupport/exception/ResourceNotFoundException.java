package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when a requested database record cannot be found.
 *
 * <p>
 * This custom runtime exception is thrown when the application
 * attempts to retrieve a requested resource or database record
 * that does not exist.
 * </p>
 *
 * <p>
 * The global exception handler can catch this exception and convert
 * it into a suitable HTTP error response for the client.
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new {@code ResourceNotFoundException} with the specified
     * error message.
     *
     * @param message description of the requested resource or database
     *                record that could not be found
     */
    public ResourceNotFoundException(String message) {

        super(message);

    }

}
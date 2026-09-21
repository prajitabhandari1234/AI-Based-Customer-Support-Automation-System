package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when a request contains invalid application data.
 *
 * <p>
 * This custom runtime exception is thrown when a client request
 * contains invalid or unacceptable data that cannot be processed
 * by the application.
 * </p>
 *
 * <p>
 * The global exception handler can catch this exception and convert
 * it into a suitable HTTP error response for the client.
 * </p>
 */
public class BadRequestException extends RuntimeException {

    /**
     * Creates a new {@code BadRequestException} with the specified
     * error message.
     *
     * @param message description of the invalid request or application data
     */
    public BadRequestException(String message) {

        super(message);

    }

}
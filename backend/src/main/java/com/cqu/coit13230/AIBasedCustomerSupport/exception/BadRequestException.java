package com.cqu.coit13230.AIBasedCustomerSupport.exception;

/**
 * Used when a request contains invalid application data.
 * The global exception handler converts it into a suitable HTTP error response.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}

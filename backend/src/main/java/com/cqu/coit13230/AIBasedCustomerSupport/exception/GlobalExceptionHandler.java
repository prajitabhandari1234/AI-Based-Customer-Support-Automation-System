package com.cqu.coit13230.AIBasedCustomerSupport.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts application exceptions into API error responses.
 *
 * <p>
 * This global exception handler provides centralized handling for
 * exceptions raised while processing REST API requests in the
 * customer support system.
 * </p>
 *
 * <p>
 * It converts application and framework exceptions into consistent
 * HTTP responses containing a timestamp, HTTP status code, error
 * description, and message instead of returning raw exceptions
 * directly to the frontend.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors produced when request data fails
     * Jakarta Bean Validation rules.
     *
     * <p>
     * Validation errors are collected by field name and added to the
     * response under the {@code validationErrors} property.
     * </p>
     *
     * @param ex exception containing the request validation errors
     * @return HTTP 400 Bad Request response containing the validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = errorBody(HttpStatus.BAD_REQUEST, "Validation failed");

        body.put("validationErrors", errors);

        return ResponseEntity.badRequest().body(body);

    }

    /**
     * Handles requests containing malformed or unreadable request bodies.
     *
     * <p>
     * This can occur when the submitted request body cannot be correctly
     * converted into the expected Java object.
     * </p>
     *
     * @param ex exception raised when the request body cannot be read
     * @return HTTP 400 Bad Request response describing the invalid request body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedRequest(HttpMessageNotReadableException ex) {

        return ResponseEntity.badRequest().body(errorBody(HttpStatus.BAD_REQUEST, "Invalid request body"));

    }

    /**
     * Handles requests containing invalid application data.
     *
     * <p>
     * Both {@link IllegalArgumentException} and
     * {@link BadRequestException} are converted into an HTTP
     * 400 Bad Request response.
     * </p>
     *
     * @param ex exception containing details about the invalid request
     * @return HTTP 400 Bad Request response containing the exception message
     */
    @ExceptionHandler({ IllegalArgumentException.class, BadRequestException.class })
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {

        return ResponseEntity.badRequest().body(errorBody(HttpStatus.BAD_REQUEST, ex.getMessage()));

    }

    /**
     * Handles requests for application resources that cannot be found.
     *
     * @param ex exception containing details about the missing resource
     * @return HTTP 404 Not Found response containing the exception message
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(HttpStatus.NOT_FOUND, ex.getMessage()));

    }

    /**
     * Handles attempts to create or use a resource that already exists.
     *
     * @param ex exception containing details about the duplicate resource
     * @return HTTP 409 Conflict response containing the exception message
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(HttpStatus.CONFLICT, ex.getMessage()));

    }

    /**
     * Handles authentication failures.
     *
     * <p>
     * Authentication failures are converted into an HTTP
     * 401 Unauthorized response.
     * </p>
     *
     * @param ex exception containing details about the authentication failure
     * @return HTTP 401 Unauthorized response containing the exception message
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(HttpStatus.UNAUTHORIZED, ex.getMessage()));

    }

    /**
     * Handles operations that cannot be performed because the account
     * is inactive or the current user does not have permission.
     *
     * <p>
     * Both {@link AccountInactiveException} and
     * {@link ForbiddenOperationException} are converted into an HTTP
     * 403 Forbidden response.
     * </p>
     *
     * @param ex exception describing why the operation is forbidden
     * @return HTTP 403 Forbidden response containing the exception message
     */
    @ExceptionHandler({ AccountInactiveException.class, ForbiddenOperationException.class })
    public ResponseEntity<Map<String, Object>> handleForbidden(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(HttpStatus.FORBIDDEN, ex.getMessage()));

    }

    /**
     * Handles unexpected exceptions that are not processed by a more
     * specific exception handler.
     *
     * <p>
     * A generic message is returned instead of exposing internal
     * exception information to the frontend.
     * </p>
     *
     * @param ex unexpected exception raised while processing the request
     * @return HTTP 500 Internal Server Error response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"));

    }

    /**
     * Builds the standard response body used for handled API errors.
     *
     * <p>
     * The generated response contains the current timestamp, numerical
     * HTTP status code, HTTP reason phrase, and the supplied error message.
     * </p>
     *
     * @param status  HTTP status associated with the error
     * @param message message describing the error
     * @return a map containing the standard API error response structure
     */
    private Map<String, Object> errorBody(HttpStatus status, String message) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());

        body.put("status", status.value());

        body.put("error", status.getReasonPhrase());

        body.put("message", message);

        return body;

    }

}
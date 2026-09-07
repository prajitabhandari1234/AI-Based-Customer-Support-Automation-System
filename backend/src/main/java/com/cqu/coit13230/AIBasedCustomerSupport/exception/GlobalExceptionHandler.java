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
 * Global exception handler for REST API errors.
 *
 * <p>
 * Converts validation, authentication, authorization, application,
 * and unexpected exceptions into consistent structured JSON responses.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors produced by {@code @Valid} request bodies.
     *
     * @param ex validation exception
     * @return structured HTTP 400 validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()));

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles malformed JSON request bodies and invalid request values,
     * including values that cannot be converted to enum constants.
     *
     * @param ex unreadable request body exception
     * @return structured HTTP 400 bad request response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedRequest(
            HttpMessageNotReadableException ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put(
                "message",
                "The request body is missing, malformed, or contains an invalid value.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles invalid business input supplied to service operations.
     *
     * <p>
     * This includes cases where a request is syntactically valid but
     * does not satisfy required business rules.
     * </p>
     *
     * @param ex illegal argument exception
     * @return structured HTTP 400 bad request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles requests for resources that do not exist.
     *
     * @param ex resource not found exception
     * @return structured HTTP 404 error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * Handles attempts to create resources that already exist.
     *
     * @param ex duplicate resource exception
     * @return structured HTTP 409 conflict response
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(
            DuplicateResourceException ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    /**
     * Handles authentication failures caused by invalid login credentials
     * or an account that is not permitted to authenticate.
     *
     * @param ex authentication exception
     * @return structured HTTP 401 unauthorized response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Handles authentication attempts made by inactive user accounts.
     *
     * @param ex account inactive exception
     * @return structured HTTP 403 forbidden response
     */
    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleAccountInactive(
            AccountInactiveException ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    /**
     * Handles operations that are forbidden for the authenticated user.
     *
     * @param ex forbidden operation exception
     * @return structured HTTP 403 forbidden response
     */
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenOperation(
            ForbiddenOperationException ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    /**
     * Handles unexpected application errors that are not covered by a more
     * specific exception handler.
     *
     * <p>
     * Internal exception details are intentionally not returned to the
     * client to avoid exposing implementation details or stack traces.
     * </p>
     *
     * @param ex unexpected exception
     * @return structured HTTP 500 internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception ex) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put(
                "status",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put(
                "message",
                "An unexpected error occurred while processing the request.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
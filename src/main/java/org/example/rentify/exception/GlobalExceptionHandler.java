package org.example.rentify.exception;

import jakarta.validation.ConstraintViolationException;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataAccessException;

import java.util.stream.Collectors;

/**
 * Global exception handler for the Rentify application.
 * This class handles various exceptions that may occur during the execution of the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResponseStatusException and returns a custom error message.
     *
     * @param ex the ResponseStatusException
     * @return a ResponseEntity with the error message
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<MessageResponseDTO> handleResponseStatusException(ResponseStatusException ex) {
        logger.warn("ResponseStatusException occurred: Status {}, Reason: {}", ex.getStatusCode(), ex.getReason(), ex);
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new MessageResponseDTO(ex.getReason() != null ? ex.getReason() : "Error processing request."));
    }

    /**
     * Handles ConstraintViolationException and returns a custom error message.
     *
     * @param ex the ConstraintViolationException
     * @return a ResponseEntity with the error message
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponseDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String propertyPath = cv.getPropertyPath().toString();
                    // Uproszczenie nazwy pola, np. z "findRoleByName.name" na "name"
                    int dotIndex = propertyPath.lastIndexOf('.');
                    if (dotIndex != -1) {
                        propertyPath = propertyPath.substring(dotIndex + 1);
                    }
                    return propertyPath + ": " + cv.getMessage();
                })
                .collect(Collectors.joining(", "));
        logger.warn("Constraint violation (parameter/path): {}", errors, ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponseDTO("Validation Error: " + errors));
    }

    /**
     * Handles IllegalArgumentException and returns a custom error message.
     *
     * @param ex the IllegalArgumentException
     * @return a ResponseEntity with the error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Illegal argument provided: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponseDTO(ex.getMessage()));
    }

    /**
     * Handles MethodArgumentNotValidException and returns a custom error message.
     *
     * @param ex the MethodArgumentNotValidException
     * @return a ResponseEntity with the error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logger.warn("Validation error: {}", errors, ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponseDTO("Validation Error: " + errors));
    }

    /**
     * Handles HttpMessageNotReadableException and returns a custom error message.
     *
     * @param ex the HttpMessageNotReadableException
     * @return a ResponseEntity with the error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON request: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponseDTO("Error: Malformed JSON request. Please check the JSON syntax."));
    }

    /**
     * Handles DataAccessException and returns a custom error message.
     *
     * @param ex the DataAccessException
     * @return a ResponseEntity with the error message
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<MessageResponseDTO> handleDataAccessException(DataAccessException ex) {
        logger.error("A database access error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponseDTO("Error: A database error occurred. Please try again later."));
    }


    /**
     * Handles MethodArgumentTypeMismatchException which occurs when a method argument
     * is not the expected type (e.g., failed enum conversion from request param).
     *
     * @param ex the MethodArgumentTypeMismatchException
     * @return a ResponseEntity with a BAD_REQUEST status and a descriptive error message.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MessageResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown type";
        String errorMessage = String.format(
                "Parameter '%s' has an invalid value: '%s'. Expected type: '%s'.",
                ex.getName(),
                ex.getValue(),
                requiredType
        );
        logger.warn("Method argument type mismatch: {}", errorMessage, ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponseDTO("Validation Error: " + errorMessage));
    }

    /**
     * Handles AccessDeniedException and returns a custom error message.
     *
     * @param ex the AccessDeniedException
     * @return a ResponseEntity with an appropriate error message and FORBIDDEN status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access Denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new MessageResponseDTO("Error: Access denied. You do not have permission to access this resource."));
    }

    /**
     * Handles generic exceptions and returns a custom error message.
     *
     * @param ex the Exception
     * @return a ResponseEntity with the error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponseDTO("Error: An unexpected internal server error occurred."));
    }
}

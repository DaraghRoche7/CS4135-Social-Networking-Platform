package com.studyhub.coreservice.common.api;

import com.studyhub.coreservice.auth.application.InvalidCredentialsException;
import com.studyhub.coreservice.auth.application.UserNotFoundException;
import com.studyhub.coreservice.post.application.PostNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
        InvalidCredentialsException ex,
        HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
        UserNotFoundException ex,
        HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePostNotFound(
        PostNotFoundException ex,
        HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(
        Exception ex,
        HttpServletRequest request
    ) {
        String message = "Validation failed";
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            message = methodArgumentNotValidException.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "%s %s".formatted(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        }
        return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
        AccessDeniedException ex,
        HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
        Exception ex,
        HttpServletRequest request
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path
        ));
    }
}

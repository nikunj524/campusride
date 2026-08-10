package com.campusride.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            fieldErrors.put(field, error.getDefaultMessage());
        });
        return response(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
    }

    @ExceptionHandler({InvalidCredentialsException.class, UserNotFoundException.class})
    public ResponseEntity<ApiError> handleUnauthorized(RuntimeException exception) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ForbiddenRegistrationRoleException.class)
    public ResponseEntity<ApiError> handleForbiddenRole(ForbiddenRegistrationRoleException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MailSendingException.class)
    public ResponseEntity<ApiError> handleMailSending(MailSendingException exception) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage(), Map.of());
    }

    @ExceptionHandler({InvalidPasswordResetOtpException.class, PasswordResetNotVerifiedException.class})
    public ResponseEntity<ApiError> handlePasswordReset(RuntimeException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, Map<String, String> fieldErrors) {
        ApiError error = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, fieldErrors);
        return ResponseEntity.status(status).body(error);
    }
}

package ru.practicum.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.hibernate.exception.ConstraintViolationException;
import ru.practicum.exception.ConstraintException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(Exception e) {
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            String message = ex.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> String.format("Field: %s. Error: %s. Value: %s",
                            fieldError.getField(),
                            fieldError.getDefaultMessage(),
                            fieldError.getRejectedValue()))
                    .findFirst()
                    .orElse("Validation failed");

            return buildApiError(
                    "BAD_REQUEST",
                    "Incorrectly made request.",
                    message
            );
        } else if (e instanceof ValidationException) {
            return buildApiError(
                    "BAD_REQUEST",
                    "Incorrectly made request.",
                    e.getMessage()
            );
        }
        return buildApiError(
                "BAD_REQUEST",
                "Incorrectly made request.",
                "Validation error occurred"
        );
    }

    @ExceptionHandler({ConstraintViolationException.class,
            DataIntegrityViolationException.class,
            ConstraintException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(Exception e) {
        return buildApiError(
                "CONFLICT",
                "Integrity constraint has been violated.",
                e.getMessage()
        );
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(Exception e) {
        return buildApiError(
                "NOT_FOUND",
                "The required object was not found.",
                e.getMessage()
        );
    }

    private ApiError buildApiError(String status, String reason, String message) {
        return new ApiError(
                status,
                reason,
                message,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        );
    }

    public static class ApiError {
        private final String status;
        private final String reason;
        private final String message;
        private final String timestamp;

        public ApiError(String status, String reason, String message, String timestamp) {
            this.status = status;
            this.reason = reason;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public String getReason() {
            return reason;
        }

        public String getMessage() {
            return message;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}
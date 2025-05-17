package ru.practicum.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.InternalServerException;
import ru.practicum.exception.NotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.exception.ValidationException;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerException(final InternalServerException e) {
        log.info("500 {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.info("404 {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final Exception e) {
        log.info("400 {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ErrorResponse handleAdditionalValidationException(final Exception e) {
        log.info("409 {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(final ForbiddenException e) {
        log.info("403 {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    private static class ErrorResponse {
        public String error;

        public ErrorResponse(String message) {
            this.error = message;
        }
    }
}
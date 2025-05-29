package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class ConstraintException extends RuntimeException {
    public ConstraintException(String message) {
        super(message);
  }
}
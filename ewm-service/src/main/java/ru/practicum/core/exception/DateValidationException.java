package ru.practicum.core.exception;

public class DateValidationException extends RuntimeException {
    public DateValidationException(String message) {
        super(message);
    }
}

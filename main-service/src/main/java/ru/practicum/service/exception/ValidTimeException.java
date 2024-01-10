package ru.practicum.service.exception;

public class ValidTimeException extends RuntimeException {
    public ValidTimeException(String message) {
        super(message);
    }
}

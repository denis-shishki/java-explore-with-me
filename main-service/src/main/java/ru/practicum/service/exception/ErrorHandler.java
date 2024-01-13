package ru.practicum.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    //todo нормально назвать методы
    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationException(final MethodArgumentNotValidException e) {
        log.warn("Validation exception: ", e);
        return new ApiError(HttpStatus.BAD_REQUEST.toString(),
                "Incorrectly made request.",
                e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError validationException(final DataIntegrityViolationException e) {
        log.warn("Validation exception: ", e);
        return new ApiError(HttpStatus.CONFLICT.toString(),
                "Integrity constraint has been violated.",
                e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError throwableException(final NotFoundException e) {
        return new ApiError(HttpStatus.NOT_FOUND.toString(),
                "The required object was not found.",
                e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationPaginatorException(final ValidationException e) {
        return new ApiError(HttpStatus.NOT_FOUND.toString(),
                "Incorrectly made request.",
                e.getMessage(),
                LocalDateTime.now());
    }
}

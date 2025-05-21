package ru.practicum.core.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.CONFLICT.toString())
                .reason("Запрошенный объект не найден.")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMet(final ConditionsNotMetException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.CONFLICT.toString())
                .reason("Нарушены условия целостности данных.")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRequest(final MethodArgumentNotValidException e) {
        String field = Objects.requireNonNull(e.getBindingResult().getFieldError()).getField();
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.CONFLICT.toString())
                .reason("Некорректный запрос.")
                .message("Некорректное значение параметра " + field + ": " + errorMessage)
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(final Throwable e) {
        ApiError apiError = ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .reason("Внутренняя ошибка сервера.")
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();

        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            apiError.setMessage(e.getMessage());
        } else {
            apiError.setMessage("Произошла непредвиденная ошибка.");
        }
        return apiError;
    }
}

package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    private static final String REASON_UNEXPECTED = "Unexpected error.";
    private static final String REASON_BAD = "Incorrectly made request.";
    private static final String NOT_FOUND = "The required object was not found.";


    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(BadRequestException e) {
        log.warn("Bad request: {}", e.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                REASON_BAD,
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(ValidationException e) {
        log.warn("ValidationException: {}", e.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                REASON_BAD,
                e.getMessage(),
                LocalDateTime.now());
    }


    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.warn("ConflictException: {}", e.getMessage());
        return new ApiError(
                HttpStatus.CONFLICT,
                "Conflict during request processing.",
                e.getMessage(),
                LocalDateTime.now());
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.warn("NotFoundException: {}", e.getMessage());
        return new ApiError(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                e.getMessage(),
                LocalDateTime.now());
    }


    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadInput(Exception e) {
        log.warn("Bad input: {}", e.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                REASON_BAD,
                e.getMessage(),
                LocalDateTime.now());
    }


    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResource(NoResourceFoundException e) {
        log.warn("No resource found: {}", e.getMessage());
        return new ApiError(
                HttpStatus.NOT_FOUND,
                NOT_FOUND,
                e.getMessage(),
                LocalDateTime.now());
    }


    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServer(InternalServerException e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error during data processing.",
                e.getMessage(),
                LocalDateTime.now()
        );
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception e) {
        log.error("Unexpected server error: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                REASON_UNEXPECTED,
                e.getMessage(),
                LocalDateTime.now());
    }


}

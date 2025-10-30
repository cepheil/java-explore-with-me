package ru.practicum.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String REASON_BAD = "Incorrectly made request.";
    private static final String REASON_UNEXPECTED = "Unexpected error.";


    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(ValidationException e) {
        log.warn("ValidationException: {}", e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, REASON_BAD, e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.warn("ConflictException: {}", e.getMessage());
        return new ApiError(HttpStatus.CONFLICT, "Conflict during request processing.",
                e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception e) {
        log.error("Unexpected server error: {}", e.getMessage(), e);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, REASON_UNEXPECTED, e.getMessage(), LocalDateTime.now());
    }


}

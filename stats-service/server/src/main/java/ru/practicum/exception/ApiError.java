package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.http.HttpStatus;
import ru.practicum.dto.DateTimeConstants;

import java.time.LocalDateTime;

@Data
public class ApiError {
    private final String status;
    private final String reason;
    private final String message;
    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private final LocalDateTime timestamp;

    public ApiError(HttpStatus status, String reason, String message, LocalDateTime timestamp) {
        this.status = status.name();
        this.reason = reason;
        this.message = message;
        this.timestamp = timestamp;
    }
}

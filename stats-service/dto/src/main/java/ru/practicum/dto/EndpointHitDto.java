package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {

    private Long id;

    @NotBlank
    @Size(max = 100, message = "app must not exceed 100 characters")
    private String app;

    @NotBlank
    @Size(max = 255, message = "uri must not exceed 255 characters")
    private String uri;

    @NotBlank
    @Size(max = 64, message = "ip must not exceed 64 characters")
    private String ip;

    @NotNull
    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime timestamp;

}

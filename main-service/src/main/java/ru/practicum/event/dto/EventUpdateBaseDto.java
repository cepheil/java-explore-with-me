package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;


@Data
public class EventUpdateBaseDto {
    @Size(min = 20, max = 2000, message = "annotation length must be between 20 and 2000 characters")
    private String annotation;

    @Size(min = 20, max = 7000, message = "description length must be between 20 and 7000 characters")
    private String description;

    @Size(min = 3, max = 120, message = "title length must be between 3 and 120 characters")
    private String title;

    private Long category;

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "participantLimit must be >= 0")
    private Integer participantLimit;

    private Boolean requestModeration;
}

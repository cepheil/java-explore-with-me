package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.utill.DateTimeConstants;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "annotation must not be empty")
    @Size(min = 20, max = 2000, message = "annotation length must be between 20 and 2000 characters")
    private String annotation;

    @NotBlank(message = "description must not be empty")
    @Size(min = 20, max = 7000, message = "description length must be between 20 and 7000 characters")
    private String description;

    @NotBlank(message = "title must not be empty")
    @Size(min = 3, max = 120, message = "title length must be between 3 and 120 characters")
    private String title;

    @NotNull(message = "category id must not be null")
    private Long category;

    @NotNull(message = "eventDate must not be null")
    @Future(message = "eventDate must be in the future")
    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime eventDate;


    @NotNull(message = "location must not be null")
    private LocationDto location;

    private Boolean paid = false;

    @PositiveOrZero(message = "participantLimit must be >= 0")
    private Integer participantLimit = 0;

    private Boolean requestModeration;

}

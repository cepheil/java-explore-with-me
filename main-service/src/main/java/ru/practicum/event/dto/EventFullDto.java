package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.enums.EventState;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.utill.DateTimeConstants;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private Long id;
    private String title;
    private String annotation;
    private String description;

    private CategoryDto category;
    private UserShortDto initiator;
    private LocationDto location;

    private EventState state;

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime eventDate;

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime publishedOn;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    private Integer confirmedRequests;
    private Integer views;

}

package ru.practicum.event.dto.param;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventParam {
    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime rangeEnd;

    @Builder.Default
    private Boolean onlyAvailable = false;

    @Builder.Default
    private String sort = "EVENT_DATE";

    @Builder.Default
    private int from = 0;

    @Builder.Default
    private int size = 10;

}

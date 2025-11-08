package ru.practicum.event.dto.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.event.enums.EventState;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEventParam {
    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;

    @DateTimeFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime rangeEnd;

    @Builder.Default
    private int from = 0;

    @Builder.Default
    private int size = 10;
}

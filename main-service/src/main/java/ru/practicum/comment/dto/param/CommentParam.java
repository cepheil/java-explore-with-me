package ru.practicum.comment.dto.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.exception.BadRequestException;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;


@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentParam {

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime rangeStart;

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime rangeEnd;

    private int from;
    private int size;

    public void validate() {
        if (from < 0 || size <= 0) {
            log.warn("Invalid pagination params: from={}, size={}", from, size);
            throw new BadRequestException("Invalid pagination params: from >= 0 and size > 0 are required");
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            log.warn("Invalid date range: start={} after end={}", rangeStart, rangeEnd);
            throw new BadRequestException("Invalid date range: rangeStart must be before or equal to rangeEnd");
        }
    }

}

package ru.practicum.comment.dto.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;


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


}

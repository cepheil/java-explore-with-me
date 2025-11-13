package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private Long eventId;
    private Long authorId;
    private String authorName;
    private String text;

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime created;

    @JsonFormat(pattern = DateTimeConstants.PATTERN)
    private LocalDateTime updated;
}

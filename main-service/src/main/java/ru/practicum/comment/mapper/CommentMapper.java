package ru.practicum.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentTextDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    public static Comment toEntity(Event event, User author, CommentTextDto dto) {
        if (dto == null) return null;

        return Comment.builder()
                .event(event)
                .author(author)
                .text(dto.getText().trim())
                .created(LocalDateTime.now())
                .build();
    }


    public static Comment applyUpdate(Comment target, CommentTextDto dto) {
        if (dto == null || target == null) return target;

        String text = dto.getText();
        if (text != null) {
            text = text.trim();
            if (!text.isEmpty() && !text.equals(target.getText())) {
                target.setText(text);
                target.setUpdated(LocalDateTime.now());
            }
        }
        return target;
    }

    public static CommentDto toDto(Comment c, String authorName) {
        if (c == null) return null;

        return new CommentDto(
                c.getId(),
                c.getEvent().getId(),
                c.getAuthor().getId(),
                authorName,
                c.getText(),
                c.getCreated(),
                c.getUpdated()
        );
    }

}




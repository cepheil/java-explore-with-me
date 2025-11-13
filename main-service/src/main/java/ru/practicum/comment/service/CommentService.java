package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentTextDto;
import ru.practicum.comment.dto.param.CommentParam;


import java.util.List;


public interface CommentService {

    List<CommentDto> getEventComments(Long eventId, Long authorId, CommentParam params);

    List<CommentDto> getUserComments(Long userId, CommentParam params);

    CommentDto create(Long userId, Long eventId, CommentTextDto dto);

    CommentDto update(Long userId, Long commentId, CommentTextDto dto);

    void deleteOwn(Long userId, Long commentId);

    void deleteByAdmin(Long commentId);

}

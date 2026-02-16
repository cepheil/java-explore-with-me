package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentTextDto;
import ru.practicum.comment.dto.param.CommentParam;
import ru.practicum.comment.service.CommentService;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // PUBLIC
    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getEventComments(@PathVariable @Positive Long eventId,
                                             @RequestParam(required = false) @Positive Long authorId,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime rangeStart,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime rangeEnd,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {

        CommentParam params = CommentParam.builder()
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        log.info("GET /events/{}/comments?authorId={}&from={}&size={}&range=({}, {})",
                eventId, authorId, from, size, rangeStart, rangeEnd);

        return commentService.getEventComments(eventId, authorId, params);
    }

    // PRIVATE
    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> getUserComments(@PathVariable @Positive Long userId,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime rangeStart,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime rangeEnd,
                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {

        CommentParam params = CommentParam.builder()
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        log.info("GET /users/{}/comments?from={}&size={}&range=({}, {})",
                userId, from, size, rangeStart, rangeEnd);

        return commentService.getUserComments(userId, params);
    }

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long eventId,
                             @Valid @RequestBody CommentTextDto dto) {

        log.info("POST /users/{}/events/{}/comments", userId, eventId);
        return commentService.create(userId, eventId, dto);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public CommentDto update(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long commentId,
                             @Valid @RequestBody CommentTextDto dto) {

        log.info("PATCH /users/{}/comments/{}", userId, commentId);
        return commentService.update(userId, commentId, dto);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwn(@PathVariable @Positive Long userId,
                          @PathVariable @Positive Long commentId) {

        log.info("DELETE /users/{}/comments/{}", userId, commentId);
        commentService.deleteOwn(userId, commentId);
    }

    //ADMIN
    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable @Positive Long commentId) {
        log.info("DELETE /admin/comments/{}", commentId);
        commentService.deleteByAdmin(commentId);
    }

}

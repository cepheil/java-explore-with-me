package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentTextDto;
import ru.practicum.comment.dto.param.CommentParam;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.enums.CommentableBy;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository requestRepository;


    @Override
    public List<CommentDto> getEventComments(Long eventId, Long authorId, CommentParam params) {
        validateId(eventId);
        validatePage(params.getFrom(), params.getSize());
        TimeRange range = validateRange(params.getRangeStart(), params.getRangeEnd());
        eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published: id=" + eventId));

        if (authorId != null && !userRepository.existsById(authorId)) {
            log.warn("Author not found: id= {}", authorId);
            throw new NotFoundException("Author not found: id=" + authorId);
        }

        int page = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(
                page,
                params.getSize(),
                Sort.by(Sort.Direction.DESC, "created")
        );
        Page<Comment> pageData = commentRepository.searchEventComments(
                eventId,
                authorId,
                range.start,
                range.end,
                pageable
        );

        if (pageData.isEmpty()) return Collections.emptyList();
        Set<Long> authorIds = pageData.stream()
                .map(c -> c.getAuthor().getId())
                .collect(Collectors.toSet());
        Map<Long, String> authorNames = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<CommentDto> result = pageData.stream()
                .map(c -> CommentMapper.toDto(c, authorNames.get(c.getAuthor().getId())))
                .toList();
        log.info("Retrieved {} comments for event: {} by author ID {}", result.size(), eventId, authorId);
        return result;
    }


    @Override
    public List<CommentDto> getUserComments(Long userId, CommentParam params) {
        validateId(userId);
        validatePage(params.getFrom(), params.getSize());
        TimeRange range = validateRange(params.getRangeStart(), params.getRangeEnd());
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        int page = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(page, params.getSize(), Sort.by(Sort.Direction.DESC, "created"));
        Page<Comment> pageData = commentRepository.searchAuthorComments(
                userId,
                range.start,
                range.end,
                pageable
        );
        if (pageData.isEmpty()) return Collections.emptyList();
        String authorName = author.getName();

        List<CommentDto> result = pageData.stream()
                .map(c -> CommentMapper.toDto(c, authorName))
                .toList();
        log.info("Retrieved {} comments for author: {}", result.size(), authorName);
        return result;
    }


    @Override
    @Transactional
    public CommentDto create(Long userId, Long eventId, CommentTextDto dto) {
        validateId(userId);
        validateId(eventId);
        if (dto == null || dto.getText() == null || dto.getText().trim().isEmpty()) {
            log.warn("Empty comment text from user {}", userId);
            throw new BadRequestException("Comment text cannot be empty");
        }
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: id=" + eventId));
        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Attempt to comment unpublished event: {}", eventId);
            throw new ConflictException("Event must be published to allow comments");
        }

        checkCommentPolicy(event, userId);

        Comment entity = CommentMapper.toEntity(event, author, dto);
        Comment saved = commentRepository.save(entity);

        log.info("User {} created comment {} for event {}", userId, saved.getId(), eventId);
        return CommentMapper.toDto(saved, author.getName());
    }


    @Override
    @Transactional
    public CommentDto update(Long userId, Long commentId, CommentTextDto dto) {
        validateId(userId);
        validateId(commentId);
        if (dto == null || dto.getText() == null || dto.getText().trim().isEmpty()) {
            log.warn("Empty comment text in update: userId={}, commentId={}", userId, commentId);
            throw new BadRequestException("Comment text cannot be empty");
        }
        String newText = dto.getText().trim();
        if (newText.length() < 3 || newText.length() > 2000) {
            log.warn("incorrect comment text length in update: userId={}, commentId={}, len={}",
                    userId, commentId, newText.length());
            throw new BadRequestException("Comment text length must be between 3 and 2000 characters");
        }
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment not found or does not belong to user: id=" + commentId + ", userId=" + userId));
        if (newText.equals(comment.getText())) {
            log.debug("No-op update for comment {} by user {} (text unchanged)", commentId, userId);
            return CommentMapper.toDto(comment, comment.getAuthor().getName());
        }
        CommentMapper.applyUpdate(comment, dto);
        Comment saved = commentRepository.save(comment);

        log.info("User {} updated comment {} (event {})", userId, saved.getId(), saved.getEvent().getId());
        return CommentMapper.toDto(saved, saved.getAuthor().getName());
    }


    @Override
    @Transactional
    public void deleteOwn(Long userId, Long commentId) {
        validateId(userId);
        validateId(commentId);
        long affected = commentRepository.deleteByIdAndAuthorId(commentId, userId);
        if (affected == 0) {
            log.warn("DeleteOwn: comment not found or not owned by user. commentId={}, userId={}", commentId, userId);
            throw new NotFoundException("Comment not found or does not belong to user: id=" + commentId + ", userId=" + userId);
        }

        log.info("User {} deleted own comment {}", userId, commentId);
    }


    @Override
    @Transactional
    public void deleteByAdmin(Long commentId) {
        validateId(commentId);
        boolean exists = commentRepository.existsById(commentId);
        if (!exists) {
            log.warn("Admin tried to delete non-existing comment: id={}", commentId);
            throw new NotFoundException("Comment not found: id=" + commentId);
        }

        commentRepository.deleteById(commentId);
        log.info("Admin deleted comment {}", commentId);

    }


    private void checkCommentPolicy(Event event, Long userId) {
        CommentableBy policy = event.getCommentableBy();
        if (policy == null || policy == CommentableBy.ALL) {
            return;
        }
        boolean confirmed = requestRepository
                .existsByEventIdAndRequesterIdAndStatus(event.getId(), userId, RequestStatus.CONFIRMED);
        if (!confirmed) {
            log.warn("User {} tried to comment event {} without confirmed participation", userId, event.getId());
            throw new ConflictException("Only confirmed participants can comment this event");
        }


    }


    private void validateId(Long id) {
        if (id == null) {
            log.warn("ID cannot be null");
            throw new BadRequestException("ID cannot be null");
        }
        if (id <= 0) {
            log.warn("ID must be positive: {}", id);
            throw new BadRequestException("ID must be positive");
        }
    }


    private void validatePage(int from, int size) {
        if (from < 0 || size <= 0) {
            log.warn("Invalid pagination params: from={}, size={}", from, size);
            throw new BadRequestException("Invalid pagination params: from >= 0 and size > 0 are required");
        }
    }

    private TimeRange validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime start = (rangeStart != null) ? rangeStart : LocalDateTime.now().minusYears(100L);
        LocalDateTime end = (rangeEnd != null) ? rangeEnd : LocalDateTime.now().plusYears(100L);
        if (start.isAfter(end)) {
            log.warn("rangeStart must be before rangeEnd");
            log.warn("Invalid date range: start={} after end={}", start, end);
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }
        return new TimeRange(start, end);
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }


}

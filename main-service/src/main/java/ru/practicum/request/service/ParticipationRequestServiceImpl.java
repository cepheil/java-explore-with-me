package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final Long MAX_VALUE = (long) Integer.MAX_VALUE;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.debug("Processing request to create ParticipationRequest for user ID: {} and event ID: {}", userId, eventId);
        validateId(userId);
        validateId(eventId);
        User user = checkUser(userId);
        Event event = checkEvent(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            log.warn("User {} tried to request participation for own event {}", userId, eventId);
            throw new ConflictException("Initiator cannot request participation for own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Attempt to request participation for unpublished event {}", eventId);
            throw new ConflictException("Only PUBLISHED events accept requests");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            log.warn("Duplicate participation request for user {} and event {}", userId, eventId);
            throw new ConflictException("Duplicate participation request");
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        boolean unlimited = event.getParticipantLimit() == null || event.getParticipantLimit() == 0;
        if (!unlimited && confirmed >= event.getParticipantLimit()) {
            log.warn("Participant limit reached for event {}", eventId);
            throw new ConflictException("Participant limit reached");
        }

        RequestStatus status = (Boolean.FALSE.equals(event.getRequestModeration()) || unlimited)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Participation request created with ID: {} for user {} and event {}",
                saved.getId(), userId, eventId);
        return ParticipationRequestMapper.toDto(saved);
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.debug("Processing request to cancel ParticipationRequest  ID: {} by user ID: {}", requestId, userId);
        validateId(userId);
        validateId(requestId);

        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request not found: id=" + requestId));

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            log.warn("Cannot cancel confirmed request");
            throw new ConflictException("Cannot cancel confirmed request");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest canceledRequest = requestRepository.save(request);

        log.info("Participation request ID {} canceled by user ID {}", requestId, userId);
        return ParticipationRequestMapper.toDto(canceledRequest);
    }


    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId, int from, int size) {
        log.debug("Processing request to get participation requests for user ID: {}, from={}, size={}",
                userId, from, size);
        validateId(userId);
        validatePage(from, size);

        PageRequest page = PageRequest.of(from / size, size, Sort.by("created").descending());

        List<ParticipationRequestDto> result = requestRepository.findAllByRequesterId(userId, page)
                .map(ParticipationRequestMapper::toDto)
                .getContent();
        log.info("Retrieved {} participation requests for user ID {}", result.size(), userId);
        return result;
    }


    // INITIATOR
    @Override
    public List<ParticipationRequestDto> getEventRequests(Long initiatorId, Long eventId) {
        log.debug("Processing request to get ParticipationRequests for event ID: {} by initiator ID: {}",
                eventId, initiatorId);
        validateId(initiatorId);
        validateId(eventId);

        Event event = checkEvent(eventId);
        checkInitiator(event, initiatorId);

        List<ParticipationRequestDto> result = requestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
        log.info("Retrieved {} participation requests for event ID {} by initiator ID {}", result.size(),
                eventId, initiatorId);
        return result;
    }


    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long initiatorId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.debug("Processing request to update ParticipationRequests for event ID: {} by initiator ID: {}",
                eventId, initiatorId);
        validateId(initiatorId);
        validateId(eventId);

        if (request == null || request.getRequestIds() == null || request.getRequestIds().isEmpty()) {
            log.warn("Request IDs are empty or null");
            throw new BadRequestException("requestIds must not be empty");
        }

        if (request.getStatus() == null || (request.getStatus() != RequestStatus.CONFIRMED &&
                                            request.getStatus() != RequestStatus.REJECTED)) {
            log.warn("Invalid status: {}", request.getStatus());
            throw new BadRequestException("status must be CONFIRMED or REJECTED");
        }


        Event event = checkEvent(eventId);
        checkInitiator(event, initiatorId);

        boolean unlimited = event.getParticipantLimit() == null || event.getParticipantLimit() == 0;
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long limit = unlimited ? MAX_VALUE : event.getParticipantLimit();

        if (request.getStatus() == RequestStatus.CONFIRMED && confirmed >= limit) {
            log.warn("Participant limit reached");
            throw new ConflictException("Participant limit reached");
        }


        List<ParticipationRequest> toProcess = requestRepository.findAllById(request.getRequestIds());
        if (toProcess.isEmpty()) {
            throw new NotFoundException("Requests not found by ids");
        }

        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        for (ParticipationRequest r : toProcess) {
            if (!r.getEvent().getId().equals(eventId)) continue;
            if (r.getStatus() != RequestStatus.PENDING) {
                log.warn("Only pending requests can be modified");
                throw new ConflictException("Only pending requests can be modified");
            }
            if (request.getStatus() == RequestStatus.CONFIRMED) {
                if (confirmed >= limit) {
                    log.warn("Participant limit reached");
                    throw new ConflictException("Participant limit reached");
                }
                r.setStatus(RequestStatus.CONFIRMED);
                confirmed++;
                confirmedDtos.add(ParticipationRequestMapper.toDto(r));
            } else {
                r.setStatus(RequestStatus.REJECTED);
                rejectedDtos.add(ParticipationRequestMapper.toDto(r));
            }
        }

        requestRepository.saveAll(toProcess);
        log.info("Updated {} requests for event ID {}: {} confirmed, {} rejected",
                toProcess.size(), eventId, confirmedDtos.size(), rejectedDtos.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedDtos)
                .rejectedRequests(rejectedDtos)
                .build();
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

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: id=" + eventId));
    }

    private void checkInitiator(Event event, Long initiatorId) {
        if (!event.getInitiator().getId().equals(initiatorId)) {
            log.warn("User {} is not initiator of event {}", initiatorId, event.getId());
            throw new ConflictException("Only initiator can update requests of the event");
        }
    }


}

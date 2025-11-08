package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    // USER:
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getUserRequests(Long userId, int from, int size);


    // INITIATOR:
    List<ParticipationRequestDto> getEventRequests(Long initiatorId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long initiatorId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);

}

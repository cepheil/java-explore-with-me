package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.dto.param.AdminEventParam;
import ru.practicum.event.dto.param.PublicEventParam;

import java.util.List;

public interface EventService {


    // PUBLIC
    List<EventShortDto> publicSearch(PublicEventParam param);

    EventFullDto publicGetById(Long eventId);

    // PRIVATE (user)
    EventFullDto create(Long userId, NewEventDto dto);

    List<EventShortDto> findUserEvents(Long userId, int from, int size);

    EventFullDto findUserEvent(Long userId, Long eventId);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequestDto dto);

    // ADMIN
    List<EventFullDto> adminSearch(AdminEventParam param);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequestDto dto);

}

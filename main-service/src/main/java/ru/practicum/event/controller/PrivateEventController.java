package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequestDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto dto) {
        log.debug("Processing request to create event for user ID: {}", userId);

        EventFullDto result = eventService.create(userId, dto);
        log.info("Event with ID {} created successfully", result.getId());
        return result;
    }


    @GetMapping
    public List<EventShortDto> findUserEvents(@PathVariable Long userId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        log.debug("Processing search request with params: from={}, size={}", from, size);

        List<EventShortDto> result = eventService.findUserEvents(userId, from, size);
        log.info("Search request completed. Found {} events", result.size());
        return result;
    }


    @GetMapping("/{eventId}")
    public EventFullDto findUserEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        log.debug("Processing request to get event by user ID: {} and event ID: {}", userId, eventId);

        EventFullDto result = eventService.findUserEvent(userId, eventId);
        log.info("Event with ID {} retrieved successfully for user ID {}", eventId, userId);
        return result;
    }


    @PatchMapping("/{eventId}")
    public EventFullDto updateByUser(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @Valid @RequestBody UpdateEventUserRequestDto dto) {
        log.debug("Processing request to update event by user ID: {} and event ID: {}", userId, eventId);

        EventFullDto result = eventService.updateByUser(userId, eventId, dto);
        log.info("Event with ID {} updated successfully for user ID {}", eventId, userId);
        return result;
    }

}

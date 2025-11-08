package ru.practicum.event.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.event.dto.param.AdminEventParam;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.utill.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> search(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        log.debug("Processing search request with params: users={}, states={}, categories={}, rangeStart={}," +
                  " rangeEnd={},  from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);


        AdminEventParam p = AdminEventParam.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        List<EventFullDto> result = eventService.adminSearch(p);
        log.info("Search request completed. Found {} events", result.size());
        return result;

    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable Long eventId,
                                      @Valid @RequestBody UpdateEventAdminRequestDto dto) {

        EventFullDto result = eventService.updateByAdmin(eventId, dto);
        log.info("Event with ID {} updated successfully", eventId);
        return result;
    }

}

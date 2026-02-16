package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.InternalServerException;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEnricher {

    private final ParticipationRequestRepository requestRepository;
    private final StatClient statsClient;


    public Map<Long, Integer> loadConfirmedMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();
        return requestRepository.countConfirmedByEventIds(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(
                        ParticipationRequestRepository.ConfirmedCount::getEventId,
                        c -> c.getConfirmed().intValue()
                ));
    }


    public Map<Long, Long> loadViewsMap(List<Long> eventIds, LocalDateTime start, LocalDateTime end) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();
        TimeRange range = normalizeRange(start, end);

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();
        List<ViewStatsDto> stats = statsClient.getStats(range.start, range.end, uris, true);
        Map<Long, Long> result = new HashMap<>();
        for (ViewStatsDto s : stats) {
            String uri = s.getUri();
            int slash = uri.lastIndexOf('/');
            if (slash >= 0 && slash + 1 < uri.length()) {
                try {
                    long id = Long.parseLong(uri.substring(slash + 1));
                    long hits = (s.getHits() != null) ? s.getHits() : 0L;
                    result.merge(id, hits, Long::sum);
                } catch (NumberFormatException ignored) {
                    log.warn("Failed to parse event ID from URI: {}", uri);
                }
            }
        }
        return result;
    }


    public List<EventShortDto> enrichShortDtoList(List<Event> events, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (events.isEmpty()) return List.of();
        log.debug("Enriching to ShortDtoList {} events with range: {} - {}", events.size(), rangeStart, rangeEnd);

        TimeRange range = normalizeRange(rangeStart, rangeEnd);
        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Integer> confirmed = loadConfirmedMap(ids);
        Map<Long, Long> views = loadViewsMap(ids, range.start(), range.end());
        return events.stream()
                .map(e -> EventMapper.toShortDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0),
                        views.getOrDefault(e.getId(), 0L).intValue()
                ))
                .toList();
    }


    public List<EventFullDto> enrichFullDtoList(List<Event> events, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (events.isEmpty()) return List.of();
        log.debug("Enriching to FullDtoList {} events with range: {} - {}", events.size(), rangeStart, rangeEnd);

        TimeRange range = normalizeRange(rangeStart, rangeEnd);
        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Integer> confirmed = loadConfirmedMap(ids);
        Map<Long, Long> views = loadViewsMap(ids, range.start, range.end);
        return events.stream()
                .map(e -> EventMapper.toFullDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0),
                        views.getOrDefault(e.getId(), 0L).intValue()
                ))
                .toList();
    }


    public EventFullDto enrichFull(Event event, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<EventFullDto> list = enrichFullDtoList(List.of(event), rangeStart, rangeEnd);
        log.debug("Enriching to FullDto  {} events with range: {} - {}", event.getId(), rangeStart, rangeEnd);

        if (list.isEmpty()) {
            log.warn("Failed to enrich event toFullDto with ID: {} ", event.getId());
            throw new InternalServerException("Failed to enrich event with ID: " + event.getId());
        }
        return list.get(0);
    }


    public EventShortDto enrichShort(Event event, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<EventShortDto> list = enrichShortDtoList(List.of(event), rangeStart, rangeEnd);
        log.debug("Enriching to ShortDto  {} events with range: {} - {}", event.getId(), rangeStart, rangeEnd);

        if (list.isEmpty()) {
            log.warn("Failed to enrich event to ShortDto with ID: {} ", event.getId());
            throw new InternalServerException("Failed to enrich event with ID: " + event.getId());
        }
        return list.get(0);
    }


    private TimeRange normalizeRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime start = (rangeStart != null) ? rangeStart : LocalDateTime.now().minusYears(100L);
        LocalDateTime end = (rangeEnd != null) ? rangeEnd : LocalDateTime.now().plusYears(100L);
        if (start.isAfter(end)) {
            log.warn("rangeStart must be before rangeEnd");
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }
        return new TimeRange(start, end);
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }

}

package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventEnricher;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventEnricher enricher;


    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        log.debug("Processing request to create compilation with DTO: {}", dto);
        if (dto == null) {
            log.warn("Compilation creation failed: dto is null");
            throw new BadRequestException("Compilation DTO cannot be null");
        }

        String title = dto.getTitle().trim();
        validateTitle(title);

        Set<Event> events = resolveEvents(dto.getEvents());
        Compilation compilation = Compilation.builder()
                .title(title)
                .pinned(Boolean.TRUE.equals(dto.getPinned()))
                .events(events)
                .build();

        Compilation saved = compilationRepository.save(compilation);
        log.info("Compilation created: id={}, pinned={}, title='{}'", saved.getId(), saved.getPinned(), saved.getTitle());

        return toDtoEnriched(saved);
    }


    @Override
    @Transactional
    public void delete(Long compId) {
        log.debug("Processing request to delete compilation with ID: {}", compId);
        validateId(compId);
        Compilation comp = checkCompilation(compId);
        compilationRepository.delete(comp);
        log.info("Compilation deleted: id={}", compId);
    }


    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationDto dto) {
        log.debug("Processing request to update compilation ID: {} with DTO: {}", compId, dto);
        if (dto == null) {
            log.warn("Compilation update is failed: dto is null");
            throw new BadRequestException("UpdateCompilation DTO cannot be null");
        }
        validateId(compId);
        Compilation comp = checkCompilation(compId);

        if (dto.getTitle() != null) {
            String title = dto.getTitle().trim();
            validateTitle(title);
            comp.setTitle(title);
        }
        if (dto.getPinned() != null) {
            comp.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            Set<Event> events = resolveEvents(dto.getEvents());
            comp.setEvents(events);
        }

        Compilation saved = compilationRepository.save(comp);
        log.info("Compilation updated: id={}, pinned={}, title='{}'", saved.getId(), saved.getPinned(), saved.getTitle());

        return toDtoEnriched(saved);
    }


    // PUBLIC
    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        log.debug("Processing request to find compilations with pinned={}, from={}, size={}", pinned, from, size);
        validatePage(from, size);
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Compilation> comps = (pinned == null)
                ? compilationRepository.findAll(page).getContent()
                : compilationRepository.findAllByPinned(pinned, page).getContent();

        List<CompilationDto> result = toDtoEnrichedBulk(comps);

        log.info("Retrieved {} compilations for pinned={}", result.size(), pinned);
        return result;
    }


    @Override
    public CompilationDto getById(Long compId) {
        log.debug("Processing request to get compilation by ID: {}", compId);
        validateId(compId);
        Compilation comp = checkCompilation(compId);

        CompilationDto result = toDtoEnriched(comp);
        log.info("Compilation retrieved: id={}", compId);
        return result;
    }


    //Utill
    private Compilation checkCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: id=" + compId));
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


    private void validateTitle(String title) {
        if (title.isEmpty() || title.length() > 50) {
            log.warn("title length {} must be between 1 and 50 characters", title.length());
            throw new BadRequestException("title length must be between 1 and 50 characters");
        }
    }


    private Set<Event> resolveEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            log.debug("No event IDs provided, returning empty set");
            return new HashSet<>();
        }

        Set<Long> uniqueIds = new HashSet<>(eventIds);
        List<Event> found = eventRepository.findAllById(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            Set<Long> foundIds = found.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            List<Long> missing = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            log.warn("Events not found by IDs: {}", missing);
            throw new NotFoundException("Events not found by ids: " + missing);
        }
        log.debug("Resolved {} events for compilation", found.size());
        return new HashSet<>(found);
    }


    private CompilationDto toDtoEnriched(Compilation c) {
        if (c.getEvents() == null || c.getEvents().isEmpty()) {
            log.debug("Compilation {} has no events, returning DTO with empty event list", c.getId());
            return CompilationMapper.toDto(c, Collections.emptyMap(), Collections.emptyMap());
        }
        List<Long> ids = c.getEvents().stream()
                .map(Event::getId)
                .toList();

        log.debug("Enriching compilation {} with {} events", c.getId(), ids.size());
        Map<Long, Integer> confirmed = enricher.loadConfirmedMap(ids);
        Map<Long, Long> views = enricher.loadViewsMap(ids, null, null);
        return CompilationMapper.toDto(c, confirmed, views);
    }


    private List<CompilationDto> toDtoEnrichedBulk(List<Compilation> comps) {
        Set<Long> allEventIds = comps.stream()
                .filter(c -> c.getEvents() != null)
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getId)
                .collect(Collectors.toSet());

        Map<Long, Integer> confirmed = allEventIds.isEmpty()
                ? Collections.emptyMap()
                : enricher.loadConfirmedMap(new ArrayList<>(allEventIds));

        Map<Long, Long> views = allEventIds.isEmpty()
                ? Collections.emptyMap()
                : enricher.loadViewsMap(new ArrayList<>(allEventIds), null, null);

        return comps.stream()
                .map(c -> CompilationMapper.toDto(c, confirmed, views))
                .toList();
    }


}

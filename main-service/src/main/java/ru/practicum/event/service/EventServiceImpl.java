package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.dto.param.AdminEventParam;
import ru.practicum.event.dto.param.PublicEventParam;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.EventSpecifications;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final int MIN_HOURS_USER_CREATE_OR_UPDATE = 2;
    private static final int MIN_HOURS_ADMIN_PUBLISH = 1;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventEnricher enricher;


    // PUBLIC
    @Override
    public List<EventShortDto> publicSearch(PublicEventParam p) {
        validatePage(p.getFrom(), p.getSize());

        LocalDateTime start = (p.getRangeStart() != null) ? p.getRangeStart() : LocalDateTime.now();
        LocalDateTime end = (p.getRangeEnd() != null) ? p.getRangeEnd() : LocalDateTime.now().plusYears(100L);
        if (start.isAfter(end)) {
            log.warn("rangeStart must be before rangeEnd");
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        boolean sortByViews = "VIEWS".equalsIgnoreCase(p.getSort());

        Specification<Event> spec = Specification
                .where(EventSpecifications.publishedBetween(start, end))
                .and(EventSpecifications.textLike(p.getText()))
                .and(EventSpecifications.categoriesIn(p.getCategories()))
                .and(EventSpecifications.paidEq(p.getPaid()))
                .and(EventSpecifications.onlyAvailable(p.getOnlyAvailable()));

        if (!sortByViews) {
            PageRequest page = PageRequest.of(p.getFrom() / p.getSize(),
                    p.getSize(), Sort.by("eventDate").ascending());
            List<Event> pageEvents = eventRepository.findAll(spec, page).getContent();
            return enricher.enrichShortDtoList(pageEvents, start, end);
        }

        List<Event> allEvents = eventRepository.findAll(spec);
        List<EventShortDto> dtos = enricher.enrichShortDtoList(allEvents, start, end);

        List<EventShortDto> sorted = dtos
                .stream()
                .sorted(Comparator
                        .comparingInt(EventShortDto::getViews).reversed()                  //сортируем по убыванию просмотров
                        .thenComparing(EventShortDto::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .toList();

        int from = p.getFrom();
        if (from >= sorted.size()) return List.of();
        int to = Math.min(from + p.getSize(), sorted.size());
        return sorted.subList(from, to);
    }


    @Override
    public EventFullDto publicGetById(Long eventId) {
        validateId(eventId);
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published: id=" + eventId));

        return enricher.enrichFull(event, null, null);
    }


    //PRIVATE USER
    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        validateId(userId);
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + dto.getCategory()));
        LocalDateTime now = LocalDateTime.now();

        if (dto.getEventDate().isBefore(now.plusHours(MIN_HOURS_USER_CREATE_OR_UPDATE))) {
            log.warn("eventDate must be at least {} hours in the future", MIN_HOURS_USER_CREATE_OR_UPDATE);
            throw new BadRequestException("eventDate must be at least " + MIN_HOURS_USER_CREATE_OR_UPDATE + " hours in the future");
        }
        Event event = EventMapper.toEntity(dto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(LocationMapper.toLocation(dto.getLocation()));
        event.setState(EventState.PENDING);
        event.setCreatedOn(now);
        event.setPublishedOn(null);
        Event saved = eventRepository.save(event);
        log.info("Event created: id={} by user={}", saved.getId(), userId);
        // новое событие = счетчики по нулям.
        return EventMapper.toFullDto(saved, 0, 0);
    }


    @Override
    public List<EventShortDto> findUserEvents(Long userId, int from, int size) {
        validateId(userId);
        validatePage(from, size);
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> pageEvents = eventRepository.findAllByInitiatorId(userId, page).getContent();
        List<EventShortDto> dtos = enricher.enrichShortDtoList(pageEvents, null, null);   //все события
        return dtos;
    }


    @Override
    public EventFullDto findUserEvent(Long userId, Long eventId) {
        validateId(userId);
        validateId(eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found: id=" + eventId));

        return enricher.enrichFull(event, null, null);
    }


    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequestDto dto) {
        validateId(userId);
        validateId(eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found: id=" + eventId));

        if (event.getState() == EventState.PUBLISHED) {
            log.warn("Published event cannot be edited by user");
            throw new ConflictException("Published event cannot be edited by user");
        }

        EventMapper.applyUpdateBase(dto, event);

        if (dto.getEventDate() != null &&
            dto.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_USER_CREATE_OR_UPDATE))) {
            log.warn("eventDate must be at least {} hours in the future", MIN_HOURS_USER_CREATE_OR_UPDATE);
            throw new BadRequestException("eventDate must be at least " + MIN_HOURS_USER_CREATE_OR_UPDATE + " hours in the future");
        }

        if (dto.getLocation() != null) {
            Location loc = LocationMapper.toLocation(dto.getLocation());
            event.setLocation(loc);
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        return enricher.enrichFull(saved, null, null);
    }


    // ADMIN
    @Override
    public List<EventFullDto> adminSearch(AdminEventParam p) {
        validatePage(p.getFrom(), p.getSize());

        LocalDateTime start = (p.getRangeStart() != null) ? p.getRangeStart() : LocalDateTime.now();
        LocalDateTime end = (p.getRangeEnd() != null) ? p.getRangeEnd() : LocalDateTime.now().plusYears(100L);

        if (start.isAfter(end)) {
            log.warn("rangeStart must be before rangeEnd");
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        Specification<Event> spec = Specification
                .where(EventSpecifications.byInitiators(p.getUsers()))
                .and(EventSpecifications.byStates(p.getStates()))
                .and(EventSpecifications.categoriesIn(p.getCategories()))
                .and(EventSpecifications.between(start, end));

        PageRequest page = PageRequest.of(p.getFrom() / p.getSize(),
                p.getSize(), Sort.by("id").ascending());

        List<Event> events = eventRepository.findAll(spec, page).getContent();

        return enricher.enrichFullDtoList(events, start, end);
    }


    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequestDto dto) {
        validateId(eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: id=" + eventId));

        EventMapper.applyUpdateBase(dto, event);

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found: id=" + dto.getCategory()));
            event.setCategory(category);
        }

        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now())) {
            log.warn("Admin tries to set past eventDate: {}", dto.getEventDate());
            throw new BadRequestException("eventDate must be in the future");
        }

        if (dto.getLocation() != null) {
            Location loc = LocationMapper.toLocation(dto.getLocation());
            event.setLocation(loc);
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        log.warn("Only pending events can be published");
                        throw new ConflictException("Only pending events can be published");
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_ADMIN_PUBLISH))) {
                        log.warn("Event date must be at least  {}  hours in the future for publish", MIN_HOURS_ADMIN_PUBLISH);
                        throw new ConflictException("Event date must be at least " + MIN_HOURS_ADMIN_PUBLISH + " hours in the future for publish");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() == EventState.PUBLISHED) {
                        log.warn("Published events cannot be rejected");
                        throw new ConflictException("Published events cannot be rejected");
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }

        Event saved = eventRepository.save(event);
        return enricher.enrichFull(saved, null, null);
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

}

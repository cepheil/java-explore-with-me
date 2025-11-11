package ru.practicum.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.dto.UserShortDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {

    public static Event toEntity(NewEventDto dto) {
        if (dto == null) return null;
        return Event.builder()
                .title(trim(dto.getTitle()))
                .annotation(trim(dto.getAnnotation()))
                .description(trim(dto.getDescription()))
                .eventDate(dto.getEventDate())
                .paid(Boolean.TRUE.equals(dto.getPaid()))
                .participantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration() == null ? true : dto.getRequestModeration())
                .build();

    }

    public static EventFullDto toFullDto(Event e, int confirmedRequests, int views) {
        if (e == null) return null;

        return new EventFullDto(
                e.getId(),
                e.getTitle(),
                e.getAnnotation(),
                e.getDescription(),
                new CategoryDto(e.getCategory().getId(), e.getCategory().getName()),
                new UserShortDto(e.getInitiator().getId(), e.getInitiator().getName()),
                LocationMapper.toLocationDto(e.getLocation()),
                e.getState(),
                e.getEventDate(),
                e.getCreatedOn(),
                e.getPublishedOn(),
                e.getPaid(),
                e.getParticipantLimit(),
                e.getRequestModeration(),
                confirmedRequests,
                views
        );
    }


    public static EventShortDto toShortDto(Event e, int confirmedRequests, int views) {
        if (e == null) return null;

        return new EventShortDto(
                e.getId(),
                e.getAnnotation(),
                new CategoryDto(e.getCategory().getId(), e.getCategory().getName()),
                confirmedRequests,
                e.getEventDate(),
                new UserShortDto(e.getInitiator().getId(), e.getInitiator().getName()),
                e.getPaid(),
                e.getTitle(),
                views
        );

    }

    public static Event applyUpdateBase(EventUpdateBaseDto dto, Event target) {
        if (dto == null || target == null) return target;

        if (dto.getTitle() != null) target.setTitle(trim(dto.getTitle()));
        if (dto.getAnnotation() != null) target.setAnnotation(trim(dto.getAnnotation()));
        if (dto.getDescription() != null) target.setDescription(trim(dto.getDescription()));
        if (dto.getEventDate() != null) target.setEventDate(dto.getEventDate());
        if (dto.getPaid() != null) target.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) target.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) target.setRequestModeration(dto.getRequestModeration());

        return target;
    }


    private static String trim(String s) {
        return s == null ? null : s.trim();
    }


}

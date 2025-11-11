package ru.practicum.compilation.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {

    public static CompilationDto toDto(Compilation c, Map<Long, Integer> confirmed, Map<Long, Long> views) {
        List<EventShortDto> list = c.getEvents().stream()
                .map(e -> EventMapper.toShortDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0),
                        views.getOrDefault(e.getId(), 0L).intValue()
                ))
                .toList();

        return CompilationDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .pinned(c.getPinned())
                .events(list)
                .build();
    }

}

package ru.practicum.event.mapper;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.LocationDto;
import ru.practicum.event.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationMapper {

    public static Location toLocation(LocationDto dto) {
        if (dto == null) return null;
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public static LocationDto toLocationDto(Location entity) {
        if (entity == null) return null;
        return new LocationDto(entity.getLat(), entity.getLon());
    }

}

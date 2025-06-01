package ru.practicum.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.LocationDTO;
import ru.practicum.event.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {
    public static Location toModel(LocationDTO locationDTO) {
        return new Location(
                locationDTO.getLat(),
                locationDTO.getLon()
        );
    }

    public static LocationDTO toDto(Location location) {
        return new LocationDTO(
                location.getLat(),
                location.getLon()
        );
    }
}
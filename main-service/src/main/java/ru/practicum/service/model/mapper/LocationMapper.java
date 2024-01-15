package ru.practicum.service.model.mapper;

import ru.practicum.service.model.Location;
import ru.practicum.service.model.dto.LocationDto;

public class LocationMapper {
    public static Location toLocation(LocationDto locationDto) {
        return new Location(null, locationDto.getLat(), locationDto.getLon());
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}

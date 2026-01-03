package at.shiftcontrol.shiftservice.mapper;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.entity.Location;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LocationMapper {
    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(
            String.valueOf(location.getId()),
            location.getName(),
            location.getDescription(),
            location.getUrl(),
            location.isReadOnly()
        );
    }

    public static List<LocationDto> toLocationDto(List<Location> locations) {
        return locations.stream()
            .map(LocationMapper::toLocationDto)
            .toList();
    }
}

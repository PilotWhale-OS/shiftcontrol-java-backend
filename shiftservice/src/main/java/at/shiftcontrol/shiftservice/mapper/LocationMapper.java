package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.dto.LocationDto;
import at.shiftcontrol.shiftservice.entity.Location;

public class LocationMapper {
    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(
            location.getId(),
            location.getName(),
            location.getDescription(),
            location.getUrl()
        );
    }
}

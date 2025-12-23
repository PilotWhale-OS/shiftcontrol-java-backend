package at.shiftcontrol.shiftservice.mapper;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.LocationDto;
import at.shiftcontrol.shiftservice.entity.Location;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LocationMapper {
    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(
            String.valueOf(location.getId()),
            location.getName(),
            location.getDescription(),
            location.getUrl()
        );
    }
}

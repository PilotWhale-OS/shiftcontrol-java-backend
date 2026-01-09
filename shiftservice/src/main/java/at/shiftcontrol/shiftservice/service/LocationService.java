package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;

public interface LocationService {
    Collection<LocationDto> getAllLocationsForEvent(long eventId);

    LocationDto createLocation(long eventId, LocationModificationDto modificationDto);

    LocationDto getLocation(long locationId);

    LocationDto updateLocation(long locationId, LocationModificationDto modificationDto);

    void deleteLocation(long locationId);
}

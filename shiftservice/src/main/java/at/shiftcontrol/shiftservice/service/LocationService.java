package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;

public interface LocationService {
    Collection<LocationDto> getAllLocationsForEvent(long eventId) throws NotFoundException;

    LocationDto createLocation(long eventId, LocationModificationDto modificationDto) throws NotFoundException;

    LocationDto getLocation(String locationId) throws NotFoundException;

    LocationDto updateLocation(String locationId, LocationModificationDto modificationDto)
        throws NotFoundException;

    void deleteLocation(String locationId) throws NotFoundException;
}

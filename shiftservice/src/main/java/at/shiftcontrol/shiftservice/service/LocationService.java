package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;

public interface LocationService {
    Collection<LocationDto> getAllLocationsForEvent(long eventId) throws NotFoundException;

    LocationDto createLocation(long eventId, LocationModificationDto modificationDto) throws NotFoundException;

    LocationDto getLocation(long locationId) throws NotFoundException;

    LocationDto updateLocation(long locationId, LocationModificationDto modificationDto)
        throws NotFoundException;

    void deleteLocation(long locationId) throws NotFoundException;
}

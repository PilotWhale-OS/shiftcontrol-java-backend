package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;
import at.shiftcontrol.shiftservice.service.LocationService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationDao locationDao;
    private final SecurityHelper securityHelper;


    @Override
    public Collection<LocationDto> getAllLocationsForEvent(long eventId) throws NotFoundException {
        return List.of();
    }

    @Override
    public LocationDto createLocation(long eventId, LocationModificationDto modificationDto) throws NotFoundException {
        return null;
    }

    @Override
    public LocationDto getLocation(String locationId) throws NotFoundException {
        return null;
    }

    @Override
    public LocationDto updateLocation(String locationId, LocationModificationDto modificationDto) throws NotFoundException {
        return null;
    }

    @Override
    public void deleteLocation(String locationId) throws NotFoundException {

    }
}

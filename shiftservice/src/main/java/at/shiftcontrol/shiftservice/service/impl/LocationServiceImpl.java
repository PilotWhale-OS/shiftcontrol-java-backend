package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.service.LocationService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationDao locationDao;
    private final EventDao eventDao;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    @Override
    public LocationDto getLocation(long locationId) throws NotFoundException {
        var location = getLocationOrThrow(locationId);

        return LocationMapper.toLocationDto(location);
    }

    @Override
    public Collection<LocationDto> getAllLocationsForEvent(long eventId) throws NotFoundException {
        eventDao.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var locations = locationDao.findAllByEventId(eventId);

        return LocationMapper.toLocationDto(locations.stream().toList());
    }

    @Override
    public LocationDto createLocation(long eventId, @NonNull LocationModificationDto modificationDto) throws NotFoundException {
        // TODO ensure admin only call

        var event = eventDao.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        var newLocation = Location.builder()
            .event(event)
            .name(modificationDto.getName())
            .description(modificationDto.getDescription())
            .url(modificationDto.getUrl())
            .readOnly(false)
            .build();

        newLocation = locationDao.save(newLocation);

        //TODO publish event
        return LocationMapper.toLocationDto(newLocation);
    }


    @Override
    public LocationDto updateLocation(long locationId, @NonNull LocationModificationDto modificationDto) throws NotFoundException {
        // TODO ensure admin only call

        var location = getLocationOrThrow(locationId);

        if (location.isReadOnly()) {
            throw new BadRequestException("Cannot modify read-only location");
        }

        location.setName(modificationDto.getName());
        location.setDescription(modificationDto.getDescription());
        location.setUrl(modificationDto.getUrl());

        location = locationDao.save(location);

        //TODO publish event
        return LocationMapper.toLocationDto(location);
    }

    @Override
    public void deleteLocation(long locationId) throws NotFoundException {
        // TODO ensure admin only call

        var location = getLocationOrThrow(locationId);

        if (location.isReadOnly()) {
            throw new BadRequestException("Cannot modify read-only location");
        }

        //TODO publish event
        locationDao.delete(location);
    }

    private Location getLocationOrThrow(long locationId) throws NotFoundException {
        return locationDao.findById(locationId)
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + locationId));
    }
}

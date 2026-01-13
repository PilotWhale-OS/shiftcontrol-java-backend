package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.LocationEvent;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationSearchDto;
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
    public LocationDto getLocation(long locationId) {
        var location = getLocationOrThrow(locationId);

        return LocationMapper.toLocationDto(location);
    }

    @Override
    public Collection<LocationDto> getAllLocationsForEvent(long eventId) {
        eventDao.getById(eventId);
        var locations = locationDao.findAllByEventId(eventId);

        return LocationMapper.toLocationDto(locations.stream().toList());
    }

    @Override
    public Collection<LocationDto> searchLocations(LocationSearchDto searchDto) {
        return List.of();
    }

    @Override
    @AdminOnly
    public LocationDto createLocation(long eventId, @NonNull LocationModificationDto modificationDto) {
        var event = eventDao.getById(eventId);

        var newLocation = Location.builder()
            .event(event)
            .name(modificationDto.getName())
            .description(modificationDto.getDescription())
            .url(modificationDto.getUrl())
            .readOnly(false)
            .build();

        newLocation = locationDao.save(newLocation);

        publisher.publishEvent(LocationEvent.of(RoutingKeys.LOCATION_CREATED, newLocation));
        return LocationMapper.toLocationDto(newLocation);
    }


    @Override
    @AdminOnly
    public LocationDto updateLocation(long locationId, @NonNull LocationModificationDto modificationDto) {
        var location = getLocationOrThrow(locationId);

        if (location.isReadOnly()) {
            throw new BadRequestException("Cannot modify read-only location");
        }

        location.setName(modificationDto.getName());
        location.setDescription(modificationDto.getDescription());
        location.setUrl(modificationDto.getUrl());

        location = locationDao.save(location);

        publisher.publishEvent(LocationEvent.of(RoutingKeys.format(RoutingKeys.LOCATION_UPDATED,
            Map.of("locationId", String.valueOf(locationId))), location));
        return LocationMapper.toLocationDto(location);
    }

    @Override
    @AdminOnly
    public void deleteLocation(long locationId) {
        var location = getLocationOrThrow(locationId);

        if (location.isReadOnly()) {
            throw new BadRequestException("Cannot modify read-only location");
        }

        locationDao.delete(location);

        publisher.publishEvent(LocationEvent.of(RoutingKeys.format(RoutingKeys.LOCATION_DELETED,
            Map.of("locationId", String.valueOf(locationId))), location));
    }

    private Location getLocationOrThrow(long locationId) {
        return locationDao.getById(locationId);
    }
}

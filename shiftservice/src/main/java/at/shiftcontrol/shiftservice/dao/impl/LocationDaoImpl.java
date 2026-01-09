package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.repo.LocationRepository;

@RequiredArgsConstructor
@Component
public class LocationDaoImpl implements LocationDao {
    private final LocationRepository locationRepository;

    @Override
    public String getName() {
        return "Location";
    }

    @Override
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    @Override
    public Location save(Location entity) {
        return locationRepository.save(entity);
    }

    @Override
    public Collection<Location> saveAll(Collection<Location> entities) {
        return locationRepository.saveAll(entities);
    }

    @Override
    public void delete(Location entity) {
        locationRepository.delete(entity);
    }

    @Override
    public Collection<Location> findAllByEventId(Long eventId) {
        return locationRepository.findAllByEventId(eventId);
    }
}

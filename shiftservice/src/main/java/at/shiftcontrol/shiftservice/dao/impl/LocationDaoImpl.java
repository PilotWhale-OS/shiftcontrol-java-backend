package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.repo.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LocationDaoImpl implements LocationDao {
    private final LocationRepository locationRepository;

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

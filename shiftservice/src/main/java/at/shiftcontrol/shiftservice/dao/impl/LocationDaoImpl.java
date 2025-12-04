package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.Location;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.repo.LocationRepository;

@RequiredArgsConstructor
@Component
public class LocationDaoImpl implements LocationDao {
    private final LocationRepository locationRepository;

    @Override
    public Optional<Location> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Location save(Location entity) {
        return null;
    }

    @Override
    public void delete(Location entity) {
    }
}

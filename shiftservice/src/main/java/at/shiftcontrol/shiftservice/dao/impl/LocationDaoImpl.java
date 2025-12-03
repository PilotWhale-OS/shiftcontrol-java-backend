package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.repo.LocationRepository;

@RequiredArgsConstructor
@Component
public class LocationDaoImpl implements LocationDao {
    private final LocationRepository locationRepository;
}

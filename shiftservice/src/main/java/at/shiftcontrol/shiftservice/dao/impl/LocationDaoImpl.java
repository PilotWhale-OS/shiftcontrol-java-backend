package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.repo.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LocationDaoImpl implements LocationDao {

    private final LocationRepository locationRepository;

}

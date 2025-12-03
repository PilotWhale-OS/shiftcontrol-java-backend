package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Location;

@DataJpaTest
@Import({TestConfig.class})
public class LocationRepositoryTest {
    @Autowired
    private LocationRepository locationRepository;

    @Test
    void testGetAllLocations() {
        List<Location> locations = locationRepository.findAll();
        Assertions.assertFalse(locations.isEmpty());
    }
}

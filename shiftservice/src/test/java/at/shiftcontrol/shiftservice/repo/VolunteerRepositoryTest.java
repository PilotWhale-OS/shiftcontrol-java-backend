package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.Volunteer;
import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@DataJpaTest
@Import({TestConfig.class})
public class VolunteerRepositoryTest {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Test
    void testGetAllVolunteers() {
        List<Volunteer> volunteers = volunteerRepository.findAll();
        Assertions.assertFalse(volunteers.isEmpty());
    }

}

package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.TimeConstraint;

@DataJpaTest
@Import({TestConfig.class})
public class TimeConstraintRepositoryTest {
    @Autowired
    private TimeConstraintRepository timeConstraintRepository;

    @Test
    void testGetAllAttendanceTimeConstraints() {
        List<TimeConstraint> attendanceTimeConstraints = timeConstraintRepository.findAll();
        Assertions.assertFalse(attendanceTimeConstraints.isEmpty());
    }
}

package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.Attendance;
import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@DataJpaTest
@Import({TestConfig.class})
public class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Test
    void testGetAllAttendances() {
        List<Attendance> attendances = attendanceRepository.findAll();
        Assertions.assertFalse(attendances.isEmpty());
    }

}

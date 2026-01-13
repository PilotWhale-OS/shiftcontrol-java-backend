package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Shift;

@DataJpaTest
@Import({TestConfig.class})
public class ShiftRepositoryTest {
    @Autowired
    private ShiftRepository shiftRepository;

    @Test
    void testGetAllShifts() {
        List<Shift> shifts = shiftRepository.findAll();
        Assertions.assertFalse(shifts.isEmpty());
    }
}

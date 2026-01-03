package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.PositionSlot;

@DataJpaTest
@Import({TestConfig.class})
public class PositionSlotRepositoryTest {
    @Autowired
    private PositionSlotRepository positionSlotRepository;

    @Test
    void testGetAllPositionSlots() {
        List<PositionSlot> positionSlots = positionSlotRepository.findAll();
        Assertions.assertFalse(positionSlots.isEmpty());
    }
}

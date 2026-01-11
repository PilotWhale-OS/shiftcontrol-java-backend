package at.shiftcontrol.shiftservice.repo;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.TrustAlertType;

@DataJpaTest
@Import({TestConfig.class})
public class TrustAlertRepositoryTest {
    @Autowired
    private TrustAlertRepository trustAlertRepository;

    @Test
    void testGetAllActivities() {
        List<TrustAlert> trustAlerts = trustAlertRepository.findAll();
        Assertions.assertFalse(trustAlerts.isEmpty());
    }

    @Test
    void testSaveActivity() {
        TrustAlert trustAlert = TrustAlert.builder()
            .alertType(TrustAlertType.OVERLOAD)
            .createdAt(Instant.now())
            .positionSlot(PositionSlot.builder().id(1L).build())
            .volunteer(Volunteer.builder().id("28c02050-4f90-4f3a-b1df-3c7d27a166e6").build())
            .build();

        TrustAlert persistedTrustAlert = trustAlertRepository.save(trustAlert);

        Assertions.assertNotNull(persistedTrustAlert);
    }
}

package at.shiftcontrol.trustservice;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.trustservice.config.EmbeddedRedisConfig;
import at.shiftcontrol.trustservice.config.TrustServiceTestConfig;
import at.shiftcontrol.trustservice.service.AlertService;
import at.shiftcontrol.trustservice.service.RedisService;
import at.shiftcontrol.trustservice.service.TrustService;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    EmbeddedRedisConfig.class,
    TrustServiceTestConfig.class
})
public class TrustServiceIT {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TrustService trustService;

    @MockitoBean
    private AlertService alertService;

    @BeforeEach
    void setup() {
        redisService.getRedis().getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void testOverloadAlert() {
        String userId = "42";
        // simulate 5 signups in quick succession
        for (int i = 0; i < 5; i++) {
            PositionSlot positionSlot = PositionSlot.builder()
                .id(i)
                .build();
            trustService.handlePositionSlotJoined(
                PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                        Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                            "volunteerId", userId)),
                    positionSlot, userId));
        }

        // AlertService should have been called for OVERLOAD
        verify(alertService, atLeastOnce()).sendAlert(eq("OVERLOAD"), eq(userId));
    }
}

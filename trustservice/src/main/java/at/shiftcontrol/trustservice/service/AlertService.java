package at.shiftcontrol.trustservice.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.lib.type.TrustAlertType;

@Slf4j
@Service
public class AlertService {

    public void sendAlert(TrustAlertType alertType, String userId, String slotId) {
        log.info("Sending {} alert for user {} on slot {}", alertType, userId, slotId);
        TrustAlertDto dto = getDto(alertType, userId, slotId);

        // TODO implement

    }

    private TrustAlertDto getDto(TrustAlertType alertType, String userId, String slotId) {
        return TrustAlertDto.builder()
            .userId(userId)
            .slotId(slotId)
            .alertType(alertType)
            .createdAt(Instant.now())
            .build();
    }

}

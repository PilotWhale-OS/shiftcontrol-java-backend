package at.shiftcontrol.trustservice.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.lib.type.TrustAlertType;

@Slf4j
@Service
public class AlertService {
    private final RestClient restClient;

    @Value("${trust.shiftservice.endpoint}")
    private String endpoint;

    public AlertService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendAlert(TrustAlertType alertType, String userId, String slotId) {
        log.info("Sending {} alert for user {} on slot {}", alertType, userId, slotId);
        TrustAlertDto dto = getDto(alertType, userId, slotId);

        // TODO implement
        sendAlert(dto);
    }

    private void sendAlert(TrustAlertDto request) {
        try {
            restClient.post()
                .uri(endpoint)
                .body(request)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("failed to send alert {}", e.getCause(), e);
        }
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

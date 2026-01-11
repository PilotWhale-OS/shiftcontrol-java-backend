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

    public void sendAlert(TrustAlertType alertType, String userId) {
        log.info("Sending {} alert for user {}", alertType, userId);
        TrustAlertDto dto = getDto(alertType, userId);
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

    private TrustAlertDto getDto(TrustAlertType alertType, String userId) {
        return TrustAlertDto.builder()
            .userId(userId)
            .alertType(alertType)
            .createdAt(Instant.now())
            .build();
    }
}

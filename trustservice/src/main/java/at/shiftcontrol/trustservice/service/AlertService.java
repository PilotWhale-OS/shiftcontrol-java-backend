package at.shiftcontrol.trustservice.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AlertService {

    public void sendAlert(String alertType, String userId) {
        // TODO implement
        log.info("Sending {} alert for user {}", alertType, userId);
    }

}

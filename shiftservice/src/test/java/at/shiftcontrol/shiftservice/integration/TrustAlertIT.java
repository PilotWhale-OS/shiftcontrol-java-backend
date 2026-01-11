package at.shiftcontrol.shiftservice.integration;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.TrustAlertType;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;
import at.shiftcontrol.shiftservice.integration.config.RestITBase;
import at.shiftcontrol.shiftservice.repo.TrustAlertRepository;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrustAlertIT extends RestITBase {
    private static final String TRUST_ALERT_PATH = "/trust-alerts";

    @Autowired
    VolunteerRepository volunteerRepository;
    @Autowired
    TrustAlertRepository trustAlertRepository;

    private Volunteer volunteerA;

    @BeforeEach
    void setUp() {
        volunteerRepository.deleteAll();
        volunteerA = Volunteer.builder()
            .id("11111")
            .build();
        volunteerRepository.save(volunteerA);
    }

    @AfterEach
    void tearDown() {
        trustAlertRepository.deleteAll();
        volunteerRepository.deleteAll();
    }

    @Test
    void postTrustAlertAsAdminSucceeds() {
        TrustAlertDto trustAlertDto = TrustAlertDto.builder()
            .alertType(TrustAlertType.OVERLOAD)
            .createdAt(Instant.now())
            .userId(volunteerA.getId())
            .build();

        var result = postRequestAsAdmin(
            TRUST_ALERT_PATH,
            trustAlertDto,
            TrustAlertDisplayDto.class);

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertEquals(trustAlertDto.getCreatedAt(), result.getCreatedAt()),
            () -> assertEquals(trustAlertDto.getAlertType(), result.getAlertType())
            // () -> assertEquals(trustAlertDto.getUserId(), result.getVolunteerDto().getId())
            // TODO fix volunteer assertion when mapped
        );

    }
}

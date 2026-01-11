package at.shiftcontrol.shiftservice.integration;

import java.time.Instant;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.TrustAlertType;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;
import at.shiftcontrol.shiftservice.integration.config.RestITBase;
import at.shiftcontrol.shiftservice.repo.TrustAlertRepository;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrustAlertIT extends RestITBase {
    private static final String TRUST_ALERT_PATH = "/trust-alerts";
    private static final String TRUST_ALERT_QUERY_PARAMS = "?page=%d&size=%d";

    @Autowired
    VolunteerRepository volunteerRepository;
    @Autowired
    TrustAlertRepository trustAlertRepository;

    private Volunteer volunteerA;
    private TrustAlert trustAlertA, trustAlertB;

    @BeforeEach
    void setUp() {
        trustAlertRepository.deleteAll();
        volunteerRepository.deleteAll();

        volunteerA = Volunteer.builder()
            .id("11111")
            .build();
        volunteerA = volunteerRepository.save(volunteerA);

        trustAlertA = TrustAlert.builder()
            .alertType(TrustAlertType.OVERLOAD)
            .createdAt(Instant.now())
            .volunteer(volunteerA)
            .build();
        trustAlertA = trustAlertRepository.save(trustAlertA);
        trustAlertB = TrustAlert.builder()
            .alertType(TrustAlertType.SPAM)
            .createdAt(Instant.now())
            .volunteer(volunteerA)
            .build();
        trustAlertB = trustAlertRepository.save(trustAlertB);
    }

    @AfterEach
    void tearDown() {
        trustAlertRepository.deleteAll();
        volunteerRepository.deleteAll();
    }

    @Test
    void getTrustAlertAsAdminSucceeds() {
        long page = 0;
        long size = 1;
        var result = getRequestAsAdmin(
            TRUST_ALERT_PATH + TRUST_ALERT_QUERY_PARAMS.formatted(page, size),
            Collection.class
        );
        assertThat(result).isNotNull();
        assertFalse(result.isEmpty());
        TrustAlertDisplayDto alert = objectMapper.convertValue(result.stream().findFirst().get(), TrustAlertDisplayDto.class);
        assertAll(
            () -> assertEquals(trustAlertB.getAlertType(), alert.getAlertType()),
            () -> assertTrue(Math.abs(
                trustAlertB.getCreatedAt().toEpochMilli() - alert.getCreatedAt().toEpochMilli()) < 1),
            () -> assertEquals(trustAlertB.getVolunteer().getId(), alert.getVolunteerDto().getId())
        );
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
            () -> assertEquals(trustAlertDto.getAlertType(), result.getAlertType()),
            () -> assertEquals(trustAlertDto.getUserId(), result.getVolunteerDto().getId())
        );
    }
}

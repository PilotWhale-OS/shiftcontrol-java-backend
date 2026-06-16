package at.shiftcontrol.shiftservice.service.rewardpoints.impl;

import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.common.UniqueCodeGenerator;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.RewardPointsShareTokenDao;
import at.shiftcontrol.shiftservice.dao.RewardPointsTransactionDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardPointsServiceImplTest {
    @Mock
    private RewardPointsCalculator calculator;

    @Mock
    private RewardPointsLedgerService ledgerService;

    @Mock
    private RewardPointsShareTokenDao rewardPointsShareTokenDao;

    @Mock
    private EventDao eventDao;

    @Mock
    private VolunteerDao volunteerDao;

    @Mock
    private RewardPointsTransactionDao rewardPointsTransactionDao;

    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private UniqueCodeGenerator uniqueCodeGenerator;

    @InjectMocks
    private RewardPointsServiceImpl rewardPointsService;

    @Test
    void getRewardPointsForAllUsersOverAllEvents_usesDirectoryProfilesForExportRows() {
        var event = new Event();
        event.setId(12L);
        event.setName("Conference");
        event.setStartTime(Instant.parse("2026-01-01T10:00:00Z"));
        event.setEndTime(Instant.parse("2026-01-02T10:00:00Z"));

        var volunteer = new Volunteer();
        volunteer.setId("user-1");

        when(eventDao.findAll()).thenReturn(List.of(event));
        when(volunteerDao.findAllByEvent(12L)).thenReturn(List.of(volunteer));
        when(userDirectoryService.getUserById("user-1")).thenReturn(directoryUser("user-1", "Casey", "Coordinator"));
        when(rewardPointsTransactionDao.sumPointsByVolunteerAndEvent("user-1", 12L)).thenReturn(42L);

        var result = rewardPointsService.getRewardPointsForAllUsersOverAllEvents();

        assertThat(result).hasSize(1);
        var export = result.iterator().next();
        assertThat(export.getEvent().getName()).isEqualTo("Conference");
        assertThat(export.getVolunteerPoints()).hasSize(1);
        var volunteerPoints = export.getVolunteerPoints().iterator().next();
        assertThat(volunteerPoints.getVolunteerId()).isEqualTo("user-1");
        assertThat(volunteerPoints.getFirstName()).isEqualTo("Casey");
        assertThat(volunteerPoints.getLastName()).isEqualTo("Coordinator");
        assertThat(volunteerPoints.getEmail()).isEqualTo("user-1@example.com");
        assertThat(volunteerPoints.getRewardPoints()).isEqualTo(42);
    }

    private static DirectoryUser directoryUser(String id, String firstName, String lastName) {
        return new DirectoryUser(id, id, firstName, lastName, id + "@example.com", UserType.ASSIGNED);
    }
}

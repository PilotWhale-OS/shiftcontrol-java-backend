package at.shiftcontrol.shiftservice.service.userdirectory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.entity.UserInviteShiftPlanAccess;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.LockStatus;
import at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType;
import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserInviteRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInviteClaimServiceTest {
    @Mock
    private UserInviteRepository userInviteRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private UserAttributeProvider userAttributeProvider;

    @InjectMocks
    private UserInviteClaimService service;

    @BeforeEach
    void setUp() {
        service.init();
    }

    @Test
    void hasPendingInviteForEmail_cachesRepositoryLookupUntilInvalidated() {
        when(userInviteRepository.findAllByEmailIgnoreCaseAndStatus("future@example.com", UserInviteStatus.PENDING))
            .thenReturn(List.of(UserInvite.builder()
                .email("future@example.com")
                .status(UserInviteStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()));

        assertThat(service.hasPendingInviteForEmail(" future@example.com ")).isTrue();
        assertThat(service.hasPendingInviteForEmail("future@example.com")).isTrue();

        verify(userInviteRepository, times(1))
            .findAllByEmailIgnoreCaseAndStatus("future@example.com", UserInviteStatus.PENDING);

        service.invalidatePendingInviteEmailCache("future@example.com");
        assertThat(service.hasPendingInviteForEmail("future@example.com")).isTrue();

        verify(userInviteRepository, times(2))
            .findAllByEmailIgnoreCaseAndStatus("future@example.com", UserInviteStatus.PENDING);
    }

    @Test
    void claimPendingInvites_marksCacheFalseAfterClaimingInvite() {
        ShiftPlan shiftPlan = ShiftPlan.builder()
            .id(9L)
            .name("Planner Plan")
            .event(Event.builder().id(3L).name("Event").build())
            .lockStatus(LockStatus.SELF_SIGNUP)
            .defaultNoRolePointsPerMinute(0)
            .build();
        Role role = Role.builder()
            .id(11L)
            .name("Dispatch")
            .shiftPlan(shiftPlan)
            .selfAssignable(false)
            .rewardPointsPerMinute(1)
            .build();
        UserInvite invite = UserInvite.builder()
            .id(1L)
            .email("future@example.com")
            .status(UserInviteStatus.PENDING)
            .expiresAt(Instant.now().plusSeconds(3600))
            .pendingRoles(List.of(role))
            .build();
        invite.addPendingShiftPlanAccess(UserInviteShiftPlanAccess.builder()
            .shiftPlan(shiftPlan)
            .accessType(UserInviteShiftPlanAccessType.PLANNER)
            .build());

        when(userInviteRepository.findAllByEmailIgnoreCaseAndStatus("future@example.com", UserInviteStatus.PENDING))
            .thenReturn(List.of(invite));
        when(volunteerRepository.findById("user-1")).thenReturn(Optional.of(Volunteer.builder()
            .id("user-1")
            .planningPlans(new java.util.HashSet<>())
            .volunteeringPlans(new java.util.HashSet<>())
            .lockedPlans(new java.util.HashSet<>())
            .roles(new java.util.HashSet<>())
            .notificationSettings(new java.util.HashSet<>())
            .build()));

        assertThat(service.hasPendingInviteForEmail("future@example.com")).isTrue();

        service.claimPendingInvites(
            UserAccount.builder().id(1L).build(),
            "user-1",
            "future@example.com",
            Instant.now()
        );

        assertThat(service.hasPendingInviteForEmail("future@example.com")).isFalse();
        verify(userInviteRepository, times(2))
            .findAllByEmailIgnoreCaseAndStatus("future@example.com", UserInviteStatus.PENDING);
        verify(userAttributeProvider).invalidateUserCache("user-1");
    }

    @Test
    void claimPendingInvites_cachesNegativeLookupWhenNoInviteExists() {
        when(userInviteRepository.findAllByEmailIgnoreCaseAndStatus("missing@example.com", UserInviteStatus.PENDING))
            .thenReturn(List.of());

        service.claimPendingInvites(
            UserAccount.builder().id(1L).build(),
            "user-1",
            "missing@example.com",
            Instant.now()
        );

        assertThat(service.hasPendingInviteForEmail("missing@example.com")).isFalse();
        verify(userInviteRepository, times(1))
            .findAllByEmailIgnoreCaseAndStatus("missing@example.com", UserInviteStatus.PENDING);
        verify(volunteerRepository, never()).findById("user-1");
    }
}

package at.shiftcontrol.shiftservice.userdirectory.current;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.entity.UserInviteShiftPlanAccess;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.LockStatus;
import at.shiftcontrol.lib.type.UserAccountStatus;
import at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType;
import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.lib.type.UserProfileSource;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.repo.EventRepository;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.role.RoleRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserAccountRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserInviteRepository;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteClaimService;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({TestConfig.class, CurrentUserProfileSyncService.class, UserInviteClaimService.class})
class CurrentUserProfileSyncServiceTest {
    @Autowired
    private CurrentUserProfileSyncService currentUserProfileSyncService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ExternalIdentityRepository externalIdentityRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ShiftPlanRepository shiftPlanRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserInviteRepository userInviteRepository;

    @MockitoBean
    private CurrentSubjectProfileResolver currentSubjectProfileResolver;

    @MockitoBean
    private UserAttributeProvider userAttributeProvider;

    @MockitoBean
    private UserDirectoryService userDirectoryService;

    @Test
    void syncCurrentSubject_createsLocalUserAccountAndExternalIdentityForTrustedAdmin() {
        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            "https://id.example.test/realms/shiftcontrol",
            "fd43d584-66db-4d74-a6b0-2ca835daa0bf",
            "current.user",
            "Current",
            "User",
            "current.user@example.com",
            "https://cdn.example.test/profiles/current-user.png",
            true,
            "token-value",
            UserType.ADMIN
        ));

        var result = currentUserProfileSyncService.syncCurrentSubject();
        var persistedUserAccount = userAccountRepository.findById(result.userAccount().getId()).orElseThrow();
        var externalIdentity = externalIdentityRepository.findByIssuerAndSubject(
            "https://id.example.test/realms/shiftcontrol",
            "fd43d584-66db-4d74-a6b0-2ca835daa0bf"
        ).orElseThrow();

        Assertions.assertAll(
            () -> Assertions.assertEquals(UserAccountStatus.ACTIVE, persistedUserAccount.getStatus()),
            () -> Assertions.assertEquals("current.user", persistedUserAccount.getPreferredUsername()),
            () -> Assertions.assertEquals("Current User", persistedUserAccount.getDisplayName()),
            () -> Assertions.assertEquals("current.user@example.com", persistedUserAccount.getEmail()),
            () -> Assertions.assertEquals("https://cdn.example.test/profiles/current-user.png", persistedUserAccount.getProfile()),
            () -> Assertions.assertTrue(persistedUserAccount.isEmailVerified()),
            () -> Assertions.assertTrue(persistedUserAccount.isPlatformAdmin()),
            () -> Assertions.assertEquals(UserProfileSource.TOKEN_CLAIMS, persistedUserAccount.getLastProfileSyncSource()),
            () -> Assertions.assertNotNull(persistedUserAccount.getLastLoginAt()),
            () -> Assertions.assertNotNull(persistedUserAccount.getLastProfileSyncAt()),
            () -> Assertions.assertEquals(persistedUserAccount.getId(), externalIdentity.getUserAccount().getId()),
            () -> Assertions.assertNotNull(externalIdentity.getLastSeenAt()),
            () -> Assertions.assertFalse(volunteerRepository.existsById("fd43d584-66db-4d74-a6b0-2ca835daa0bf"))
        );
        verify(userDirectoryService).invalidateCachedUser("fd43d584-66db-4d74-a6b0-2ca835daa0bf");
    }

    @Test
    void syncCurrentSubject_updatesExistingExternalIdentityWithoutBlankingProfileFields() {
        volunteerRepository.save(emptyVolunteer("subject-123"));

        var initialResult = currentUserProfileSyncServiceSync(
            "https://id.example.test/realms/shiftcontrol",
            "subject-123",
            "initial.user",
            "Initial",
            "User",
            "initial.user@example.com",
            "https://cdn.example.test/profiles/subject-123-initial.png",
            true
        );
        Instant firstSyncAt = initialResult.userAccount().getLastProfileSyncAt();

        var updatedResult = currentUserProfileSyncServiceSync(
            "https://id.example.test/realms/shiftcontrol",
            "subject-123",
            "updated.user",
            null,
            null,
            null,
            "https://cdn.example.test/profiles/subject-123-updated.png",
            null
        );

        Assertions.assertAll(
            () -> Assertions.assertEquals(initialResult.userAccount().getId(), updatedResult.userAccount().getId()),
            () -> Assertions.assertEquals("updated.user", updatedResult.userAccount().getPreferredUsername()),
            () -> Assertions.assertEquals("Initial", updatedResult.userAccount().getFirstName()),
            () -> Assertions.assertEquals("User", updatedResult.userAccount().getLastName()),
            () -> Assertions.assertEquals("initial.user@example.com", updatedResult.userAccount().getEmail()),
            () -> Assertions.assertEquals("https://cdn.example.test/profiles/subject-123-updated.png", updatedResult.userAccount().getProfile()),
            () -> Assertions.assertTrue(updatedResult.userAccount().isEmailVerified()),
            () -> Assertions.assertTrue(updatedResult.userAccount().getLastProfileSyncAt().isAfter(firstSyncAt)
                || updatedResult.userAccount().getLastProfileSyncAt().equals(firstSyncAt)),
            () -> Assertions.assertEquals(1, externalIdentityRepository.findAllByUserAccountId(updatedResult.userAccount().getId()).size())
        );
        verify(userDirectoryService, times(2)).invalidateCachedUser("subject-123");
    }

    @Test
    void syncCurrentSubjectIfStale_returnsTransientProfileForIrrelevantAuthenticatedUser() {
        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            "https://id.example.test/realms/shiftcontrol",
            "ignored-subject-1",
            "ignored.user",
            "Ignored",
            "User",
            "ignored.user@example.com",
            "https://cdn.example.test/profiles/ignored-user.png",
            true,
            "token-value",
            UserType.ASSIGNED
        ));

        CurrentSubjectProfileSyncResult result = currentUserProfileSyncService.syncCurrentSubjectIfStale();

        Assertions.assertAll(
            () -> Assertions.assertEquals(0L, result.userAccount().getId()),
            () -> Assertions.assertEquals("ignored.user", result.userAccount().getPreferredUsername()),
            () -> Assertions.assertEquals("Ignored User", result.userAccount().getDisplayName()),
            () -> Assertions.assertEquals("ignored.user@example.com", result.userAccount().getEmail()),
            () -> Assertions.assertEquals("https://cdn.example.test/profiles/ignored-user.png", result.userAccount().getProfile()),
            () -> Assertions.assertEquals(0, userAccountRepository.count()),
            () -> Assertions.assertEquals(0, externalIdentityRepository.count()),
            () -> Assertions.assertFalse(volunteerRepository.existsById("ignored-subject-1"))
        );
    }

    @Test
    void syncCurrentSubject_marksAdminUsersAndReusesExistingPlaceholderAccount() {
        UserAccount placeholderAccount = UserAccount.builder()
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername("legacy-user")
            .displayName("legacy-user")
            .lastProfileSyncSource(UserProfileSource.MIGRATION)
            .build();
        placeholderAccount.addExternalIdentity(at.shiftcontrol.lib.entity.ExternalIdentity.builder()
            .issuer(at.shiftcontrol.shiftservice.userdirectory.LocalUserDirectoryProvisioningService.LEGACY_VOLUNTEER_ISSUER)
            .subject("subject-999")
            .createdAt(Instant.parse("2026-06-16T09:00:00Z"))
            .lastSeenAt(Instant.parse("2026-06-16T09:00:00Z"))
            .build());
        UserAccount savedPlaceholder = userAccountRepository.save(placeholderAccount);

        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            "https://id.example.test/realms/shiftcontrol",
            "subject-999",
            "admin.user",
            "Admin",
            "User",
            "admin.user@example.com",
            "https://cdn.example.test/profiles/admin-user.png",
            true,
            "token-value",
            UserType.ADMIN
        ));

        CurrentSubjectProfileSyncResult result = currentUserProfileSyncService.syncCurrentSubject();

        Assertions.assertAll(
            () -> Assertions.assertEquals(savedPlaceholder.getId(), result.userAccount().getId()),
            () -> Assertions.assertTrue(result.userAccount().isPlatformAdmin()),
            () -> Assertions.assertEquals(2, externalIdentityRepository.findAllByUserAccountId(result.userAccount().getId()).size())
        );
        verify(userDirectoryService).invalidateCachedUser("subject-999");
    }

    @Test
    void syncCurrentSubject_doesNotLinkAcrossDifferentTrustedIssuersBySubjectAlone() {
        UserAccount existingForeignAccount = UserAccount.builder()
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername("foreign-user")
            .displayName("Foreign User")
            .lastProfileSyncSource(UserProfileSource.TOKEN_CLAIMS)
            .build();
        existingForeignAccount.addExternalIdentity(at.shiftcontrol.lib.entity.ExternalIdentity.builder()
            .issuer("https://other-id.example.test/realms/shiftcontrol")
            .subject("subject-777")
            .createdAt(Instant.parse("2026-06-16T09:00:00Z"))
            .lastSeenAt(Instant.parse("2026-06-16T09:00:00Z"))
            .build());
        UserAccount savedForeignAccount = userAccountRepository.save(existingForeignAccount);
        volunteerRepository.save(emptyVolunteer("subject-777"));

        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            "https://id.example.test/realms/shiftcontrol",
            "subject-777",
            "local.user",
            "Local",
            "User",
            "local.user@example.com",
            "https://cdn.example.test/profiles/local-user.png",
            true,
            "token-value",
            UserType.ASSIGNED
        ));

        CurrentSubjectProfileSyncResult result = currentUserProfileSyncService.syncCurrentSubject();

        Assertions.assertAll(
            () -> Assertions.assertNotEquals(savedForeignAccount.getId(), result.userAccount().getId()),
            () -> Assertions.assertEquals(2, userAccountRepository.count()),
            () -> Assertions.assertTrue(externalIdentityRepository.findByIssuerAndSubject(
                "https://id.example.test/realms/shiftcontrol",
                "subject-777"
            ).isPresent())
        );
    }

    @Test
    void syncCurrentSubject_claimsPendingInviteAndMaterializesPlanAccessAndRoles() {
        Event event = eventRepository.save(Event.builder()
            .name("Invite claim event")
            .startTime(Instant.parse("2026-07-01T08:00:00Z"))
            .endTime(Instant.parse("2026-07-01T18:00:00Z"))
            .build());
        ShiftPlan shiftPlan = shiftPlanRepository.save(ShiftPlan.builder()
            .event(event)
            .name("Claimed planner plan")
            .lockStatus(LockStatus.SELF_SIGNUP)
            .defaultNoRolePointsPerMinute(0)
            .build());
        Role role = roleRepository.save(Role.builder()
            .shiftPlan(shiftPlan)
            .name("Dispatch")
            .description("Assigned at claim time")
            .selfAssignable(false)
            .rewardPointsPerMinute(3)
            .build());

        UserInvite invite = UserInvite.builder()
            .code("claim-me-001")
            .email("claim.user@example.com")
            .firstName("Claim")
            .lastName("User")
            .status(UserInviteStatus.PENDING)
            .createdAt(Instant.parse("2026-06-17T09:00:00Z"))
            .pendingRoles(List.of(role))
            .build();
        invite.addPendingShiftPlanAccess(UserInviteShiftPlanAccess.builder()
            .shiftPlan(shiftPlan)
            .accessType(UserInviteShiftPlanAccessType.PLANNER)
            .build());
        userInviteRepository.save(invite);

        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            "https://id.example.test/realms/shiftcontrol",
            "claim-subject-1",
            "claim.user",
            "Claim",
            "User",
            "claim.user@example.com",
            "https://cdn.example.test/profiles/claim-user.png",
            true,
            "token-value",
            UserType.ASSIGNED
        ));

        CurrentSubjectProfileSyncResult result = currentUserProfileSyncService.syncCurrentSubject();
        var claimedInvite = userInviteRepository.findByCode("claim-me-001").orElseThrow();
        var volunteer = volunteerRepository.findById("claim-subject-1").orElseThrow();

        Assertions.assertAll(
            () -> Assertions.assertEquals(UserInviteStatus.CLAIMED, claimedInvite.getStatus()),
            () -> Assertions.assertEquals(result.userAccount().getId(), claimedInvite.getClaimedUserAccount().getId()),
            () -> Assertions.assertNotNull(claimedInvite.getClaimedAt()),
            () -> Assertions.assertTrue(volunteer.getVolunteeringPlans().stream().anyMatch(plan -> plan.getId() == shiftPlan.getId())),
            () -> Assertions.assertTrue(volunteer.getPlanningPlans().stream().anyMatch(plan -> plan.getId() == shiftPlan.getId())),
            () -> Assertions.assertTrue(volunteer.getRoles().stream().anyMatch(savedRole -> savedRole.getId() == role.getId()))
        );
        verify(userDirectoryService).invalidateCachedUser("claim-subject-1");
    }

    private CurrentSubjectProfileSyncResult currentUserProfileSyncServiceSync(
        String issuer,
        String subject,
        String preferredUsername,
        String firstName,
        String lastName,
        String email,
        String profile,
        Boolean emailVerified
    ) {
        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            issuer,
            subject,
            preferredUsername,
            firstName,
            lastName,
            email,
            profile,
            emailVerified,
            "token-value",
            UserType.ASSIGNED
        ));
        return currentUserProfileSyncService.syncCurrentSubject();
    }

    private Volunteer emptyVolunteer(String id) {
        return Volunteer.builder()
            .id(id)
            .planningPlans(java.util.Set.of())
            .volunteeringPlans(java.util.Set.of())
            .lockedPlans(java.util.Set.of())
            .roles(java.util.Set.of())
            .notificationSettings(java.util.Set.of())
            .build();
    }
}

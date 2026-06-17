package at.shiftcontrol.shiftservice.userdirectory.current;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.type.UserAccountStatus;
import at.shiftcontrol.lib.type.UserProfileSource;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserAccountRepository;

import static org.mockito.Mockito.when;

@DataJpaTest
@Import({TestConfig.class, CurrentUserProfileSyncService.class})
class CurrentUserProfileSyncServiceTest {
    @Autowired
    private CurrentUserProfileSyncService currentUserProfileSyncService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ExternalIdentityRepository externalIdentityRepository;

    @MockitoBean
    private CurrentSubjectProfileResolver currentSubjectProfileResolver;

    @Test
    void syncCurrentSubject_createsLocalUserAccountAndExternalIdentity() {
        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            "https://id.example.test/realms/shiftcontrol",
            "fd43d584-66db-4d74-a6b0-2ca835daa0bf",
            "current.user",
            "Current",
            "User",
            "current.user@example.com",
            true,
            "token-value",
            UserType.ASSIGNED
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
            () -> Assertions.assertTrue(persistedUserAccount.isEmailVerified()),
            () -> Assertions.assertFalse(persistedUserAccount.isPlatformAdmin()),
            () -> Assertions.assertEquals(UserProfileSource.TOKEN_CLAIMS, persistedUserAccount.getLastProfileSyncSource()),
            () -> Assertions.assertNotNull(persistedUserAccount.getLastLoginAt()),
            () -> Assertions.assertNotNull(persistedUserAccount.getLastProfileSyncAt()),
            () -> Assertions.assertEquals(persistedUserAccount.getId(), externalIdentity.getUserAccount().getId()),
            () -> Assertions.assertNotNull(externalIdentity.getLastSeenAt())
        );
    }

    @Test
    void syncCurrentSubject_updatesExistingExternalIdentityWithoutBlankingProfileFields() {
        var initialResult = currentUserProfileSyncServiceSync(
            "https://id.example.test/realms/shiftcontrol",
            "subject-123",
            "initial.user",
            "Initial",
            "User",
            "initial.user@example.com",
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
            null
        );

        Assertions.assertAll(
            () -> Assertions.assertEquals(initialResult.userAccount().getId(), updatedResult.userAccount().getId()),
            () -> Assertions.assertEquals("updated.user", updatedResult.userAccount().getPreferredUsername()),
            () -> Assertions.assertEquals("Initial", updatedResult.userAccount().getFirstName()),
            () -> Assertions.assertEquals("User", updatedResult.userAccount().getLastName()),
            () -> Assertions.assertEquals("initial.user@example.com", updatedResult.userAccount().getEmail()),
            () -> Assertions.assertTrue(updatedResult.userAccount().isEmailVerified()),
            () -> Assertions.assertTrue(updatedResult.userAccount().getLastProfileSyncAt().isAfter(firstSyncAt)
                || updatedResult.userAccount().getLastProfileSyncAt().equals(firstSyncAt)),
            () -> Assertions.assertEquals(1, externalIdentityRepository.findAllByUserAccountId(updatedResult.userAccount().getId()).size())
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
    }

    private CurrentSubjectProfileSyncResult currentUserProfileSyncServiceSync(
        String issuer,
        String subject,
        String preferredUsername,
        String firstName,
        String lastName,
        String email,
        Boolean emailVerified
    ) {
        when(currentSubjectProfileResolver.resolveCurrentSubject()).thenReturn(new CurrentSubjectProfile(
            issuer,
            subject,
            preferredUsername,
            firstName,
            lastName,
            email,
            emailVerified,
            "token-value",
            UserType.ASSIGNED
        ));
        return currentUserProfileSyncService.syncCurrentSubject();
    }
}

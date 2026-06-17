package at.shiftcontrol.shiftservice.repo;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.ExternalIdentity;
import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.type.UserAccountStatus;
import at.shiftcontrol.lib.type.UserProfileSource;
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserAccountRepository;

@DataJpaTest
@Import({TestConfig.class})
class UserAccountRepositoryTest {
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ExternalIdentityRepository externalIdentityRepository;

    @Test
    void saveAndResolveUserAccountByEmailAndExternalIdentity() {
        UserAccount userAccount = UserAccount.builder()
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername("directory.user")
            .firstName("Directory")
            .lastName("User")
            .displayName("Directory User")
            .email("directory.user@example.com")
            .emailVerified(true)
            .lastLoginAt(Instant.parse("2026-06-16T09:00:00Z"))
            .lastProfileSyncAt(Instant.parse("2026-06-16T09:05:00Z"))
            .lastProfileSyncSource(UserProfileSource.TOKEN_CLAIMS)
            .build();

        userAccount.addExternalIdentity(ExternalIdentity.builder()
            .issuer("https://id.example.test/realms/shiftcontrol")
            .subject("11111111-2222-3333-4444-555555555555")
            .lastSeenAt(Instant.parse("2026-06-16T09:05:00Z"))
            .build());

        UserAccount savedUserAccount = userAccountRepository.save(userAccount);

        UserAccount resolvedByEmail = userAccountRepository.findFirstByEmailIgnoreCase("DIRECTORY.USER@example.com")
            .orElseThrow();
        ExternalIdentity externalIdentity = externalIdentityRepository.findByIssuerAndSubject(
            "https://id.example.test/realms/shiftcontrol",
            "11111111-2222-3333-4444-555555555555"
        ).orElseThrow();

        Assertions.assertAll(
            () -> Assertions.assertEquals(savedUserAccount.getId(), resolvedByEmail.getId()),
            () -> Assertions.assertEquals(savedUserAccount.getId(), externalIdentity.getUserAccount().getId()),
            () -> Assertions.assertEquals(UserProfileSource.TOKEN_CLAIMS, resolvedByEmail.getLastProfileSyncSource()),
            () -> Assertions.assertEquals(1, externalIdentityRepository.findAllByUserAccountId(savedUserAccount.getId()).size())
        );
    }

    @Test
    void findAllPlatformAdminsWithExternalIdentities_returnsOnlyMarkedAdmins() {
        UserAccount admin = UserAccount.builder()
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername("admin.user")
            .displayName("Admin User")
            .platformAdmin(true)
            .build();
        admin.addExternalIdentity(ExternalIdentity.builder()
            .issuer("https://id.example.test/realms/shiftcontrol")
            .subject("admin-1")
            .lastSeenAt(Instant.parse("2026-06-16T09:05:00Z"))
            .build());

        UserAccount assigned = UserAccount.builder()
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername("assigned.user")
            .displayName("Assigned User")
            .platformAdmin(false)
            .build();
        assigned.addExternalIdentity(ExternalIdentity.builder()
            .issuer("https://id.example.test/realms/shiftcontrol")
            .subject("user-1")
            .lastSeenAt(Instant.parse("2026-06-16T09:05:00Z"))
            .build());

        userAccountRepository.save(admin);
        userAccountRepository.save(assigned);

        var admins = userAccountRepository.findAllPlatformAdminsWithExternalIdentities();

        Assertions.assertAll(
            () -> Assertions.assertEquals(1, admins.size()),
            () -> Assertions.assertTrue(admins.get(0).isPlatformAdmin()),
            () -> Assertions.assertEquals("admin.user", admins.get(0).getPreferredUsername())
        );
    }
}

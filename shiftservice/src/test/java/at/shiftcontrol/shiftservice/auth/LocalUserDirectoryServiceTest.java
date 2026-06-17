package at.shiftcontrol.shiftservice.auth;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.entity.ExternalIdentity;
import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.type.UserAccountStatus;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserAccountRepository;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.LocalUserDirectoryProvisioningService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalUserDirectoryServiceTest {
    @Mock
    private ExternalIdentityRepository externalIdentityRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private LocalUserDirectoryProvisioningService provisioningService;

    @InjectMocks
    private LocalUserDirectoryService localUserDirectoryService;

    @Test
    void getUserById_prefersLocalProjectionBeforeProvisioningFallback() {
        localUserDirectoryService.init();
        when(externalIdentityRepository.findAllBySubjectIn(Set.of("user-1"))).thenReturn(List.of(
            externalIdentity("user-1", "Local", "User", "local.user@example.com")
        ));

        DirectoryUser user = localUserDirectoryService.getUserById("user-1");

        assertThat(user.firstName()).isEqualTo("Local");
        verify(provisioningService, never()).ensureUserAccountForVolunteerId("user-1");
    }

    @Test
    void getUserById_provisionsKnownVolunteerWhenProjectionIsMissing() {
        localUserDirectoryService.init();
        when(externalIdentityRepository.findAllBySubjectIn(Set.of("user-2"))).thenReturn(List.of());
        when(volunteerRepository.existsById("user-2")).thenReturn(true);
        when(externalIdentityRepository.findAllBySubjectIn(Set.of("user-2"))).thenReturn(
            List.of(),
            List.of(externalIdentity("user-2", "Provisioned", "User", ""))
        );

        DirectoryUser user = localUserDirectoryService.getUserById("user-2");

        assertThat(user.firstName()).isEqualTo("Provisioned");
        verify(provisioningService).ensureUserAccountForVolunteerId("user-2");
    }

    @Test
    void getAllAdmins_readsLocalPlatformAdminAccounts() {
        localUserDirectoryService.init();
        when(userAccountRepository.findAllPlatformAdminsWithExternalIdentities()).thenReturn(List.of(
            externalIdentity("admin-1", "Admin", "User", "admin.user@example.com").getUserAccount()
        ));

        var admins = localUserDirectoryService.getAllAdmins();

        assertThat(admins)
            .extracting(DirectoryUser::id, DirectoryUser::userType)
            .containsExactly(org.assertj.core.groups.Tuple.tuple("admin-1", UserType.ADMIN));
    }

    @Test
    void getUserByIds_mergesLocalAndProvisionedLookupsInRequestOrder() {
        localUserDirectoryService.init();
        when(externalIdentityRepository.findAllBySubjectIn(Set.of("user-1", "user-3"))).thenReturn(List.of(
            externalIdentity("user-1", "Local", "First", "local.first@example.com"),
            externalIdentity("user-3", "Remote", "Third", "remote.third@example.com")
        ));

        var users = localUserDirectoryService.getUserByIds(List.of("user-1", "user-3"));

        assertThat(users)
            .extracting(DirectoryUser::id)
            .containsExactly("user-1", "user-3");
    }

    private ExternalIdentity externalIdentity(String subject, String firstName, String lastName, String email) {
        UserAccount userAccount = UserAccount.builder()
            .id(1L)
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername(subject)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .platformAdmin(subject.startsWith("admin"))
            .build();

        ExternalIdentity externalIdentity = ExternalIdentity.builder()
            .id(1L)
            .subject(subject)
            .issuer("https://id.example.test/realms/shiftcontrol")
            .userAccount(userAccount)
            .build();
        userAccount.addExternalIdentity(externalIdentity);
        return externalIdentity;
    }
}

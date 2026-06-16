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
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalFirstUserDirectoryServiceTest {
    @Mock
    private ExternalIdentityRepository externalIdentityRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @InjectMocks
    private LocalFirstUserDirectoryService localFirstUserDirectoryService;

    @Test
    void getUserById_prefersLocalProjectionBeforeKeycloakFallback() {
        localFirstUserDirectoryService.init();
        when(externalIdentityRepository.findAllBySubjectIn(Set.of("user-1"))).thenReturn(List.of(
            externalIdentity("user-1", "Local", "User", "local.user@example.com")
        ));

        DirectoryUser user = localFirstUserDirectoryService.getUserById("user-1");

        assertThat(user.firstName()).isEqualTo("Local");
        verify(keycloakUserService, never()).getUserById("user-1");
    }

    @Test
    void getUserById_cachesFallbackUsersAfterFirstKeycloakLookup() {
        localFirstUserDirectoryService.init();
        when(externalIdentityRepository.findAllBySubjectIn(Set.of("user-2"))).thenReturn(List.of());
        when(keycloakUserService.getUserById("user-2")).thenReturn(new DirectoryUser(
            "user-2",
            "remote.user",
            "Remote",
            "User",
            "remote.user@example.com",
            UserType.ASSIGNED
        ));

        DirectoryUser firstLookup = localFirstUserDirectoryService.getUserById("user-2");
        DirectoryUser secondLookup = localFirstUserDirectoryService.getUserById("user-2");

        assertThat(firstLookup).isEqualTo(secondLookup);
        verify(keycloakUserService).getUserById("user-2");
    }

    @Test
    void getUserByIds_mergesLocalAndFallbackLookupsInRequestOrder() {
        localFirstUserDirectoryService.init();
        when(externalIdentityRepository.findAllBySubjectIn(Set.of("user-1", "user-3"))).thenReturn(List.of(
            externalIdentity("user-1", "Local", "First", "local.first@example.com")
        ));
        when(keycloakUserService.getUserByIds(Set.of("user-3"))).thenReturn(List.of(
            new DirectoryUser("user-3", "remote.third", "Remote", "Third", "remote.third@example.com", UserType.ASSIGNED)
        ));

        var users = localFirstUserDirectoryService.getUserByIds(List.of("user-1", "user-3"));

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
            .build();

        return ExternalIdentity.builder()
            .id(1L)
            .subject(subject)
            .issuer("https://id.example.test/realms/shiftcontrol")
            .userAccount(userAccount)
            .build();
    }
}

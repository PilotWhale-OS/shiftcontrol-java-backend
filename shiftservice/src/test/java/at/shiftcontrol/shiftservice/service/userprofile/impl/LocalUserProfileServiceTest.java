package at.shiftcontrol.shiftservice.service.userprofile.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.lib.type.UserAccountStatus;
import at.shiftcontrol.lib.type.UserProfileSource;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.service.VolunteerService;
import at.shiftcontrol.shiftservice.service.userprofile.NotificationService;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;
import at.shiftcontrol.shiftservice.userdirectory.current.CurrentSubjectProfile;
import at.shiftcontrol.shiftservice.userdirectory.current.CurrentSubjectProfileSyncResult;
import at.shiftcontrol.shiftservice.userdirectory.current.CurrentUserProfileSyncService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalUserProfileServiceTest {
    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationUserProvider userProvider;

    @Mock
    private CurrentUserProfileSyncService currentUserProfileSyncService;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private VolunteerService volunteerService;

    @InjectMocks
    private LocalUserProfileService userProfileService;

    @Test
    void getUserProfile_rejectsAccessToOtherUsersForNonAdmins() {
        var currentUser = mock(ShiftControlUser.class);
        when(currentUser.getUserId()).thenReturn("self");
        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(securityHelper.isNotUserAdmin()).thenReturn(true);

        assertThatThrownBy(() -> userProfileService.getUserProfile("other"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("cannot access other user's profile");
    }

    @Test
    void getUserProfile_fillsMissingNotificationTypesAndMapsProfile() {
        var currentUser = mock(ShiftControlUser.class);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(userProvider.getCurrentUser()).thenReturn(currentUser);

        var userAccount = UserAccount.builder()
            .id(1L)
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername("user.one")
            .firstName("User")
            .lastName("One")
            .displayName("User One")
            .email("user.one@example.com")
            .emailVerified(true)
            .lastProfileSyncSource(UserProfileSource.TOKEN_CLAIMS)
            .build();
        when(currentUserProfileSyncService.syncCurrentSubjectIfStale()).thenReturn(new CurrentSubjectProfileSyncResult(
            new CurrentSubjectProfile(
                "https://id.example.test/realms/shiftcontrol",
                "user-1",
                "user.one",
                "User",
                "One",
                "user.one@example.com",
                true,
                "token-value",
                UserType.ASSIGNED
            ),
            userAccount
        ));

        Volunteer volunteer = Volunteer.builder()
            .id("user-1")
            .roles(Set.of())
            .volunteeringPlans(Set.of())
            .planningPlans(Set.of())
            .lockedPlans(Set.of())
            .notificationSettings(Set.of())
            .build();
        when(volunteerService.getOrCreate("user-1")).thenReturn(volunteer);

        var existing = NotificationSettingsDto.builder()
            .type(NotificationType.ADMIN_TRUST_ALERT_RECEIVED)
            .channels(Set.of())
            .build();
        when(notificationService.getNotificationsForUser("user-1")).thenReturn(new ArrayList<>(List.of(existing)));

        var result = userProfileService.getUserProfile("user-1");

        assertThat(result.getAccount().getVolunteer().getId()).isEqualTo("user-1");
        assertThat(result.getAccount().getUsername()).isEqualTo("user.one");
        assertThat(result.getAccount().getEmail()).isEqualTo("user.one@example.com");
        assertThat(result.getNotifications()).hasSize(NotificationType.values().length);
        assertThat(result.getNotifications())
            .extracting(NotificationSettingsDto::getType)
            .containsExactlyInAnyOrder(NotificationType.values());

        verify(currentUserProfileSyncService).syncCurrentSubjectIfStale();
        verify(volunteerService).getOrCreate("user-1");
        verify(notificationService).getNotificationsForUser("user-1");
    }

    @Test
    void getUserProfile_forAdminViewingAnotherUserStillUsesDirectoryService() {
        var currentUser = mock(ShiftControlUser.class);
        when(currentUser.getUserId()).thenReturn("admin-user");
        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(securityHelper.isNotUserAdmin()).thenReturn(false);

        var directoryUser = new DirectoryUser(
            "other-user",
            "other.user",
            "Other",
            "User",
            "other.user@example.com",
            UserType.ASSIGNED
        );
        when(userDirectoryService.getUserById("other-user")).thenReturn(directoryUser);

        Volunteer volunteer = Volunteer.builder()
            .id("other-user")
            .roles(Set.of())
            .volunteeringPlans(Set.of())
            .planningPlans(Set.of())
            .lockedPlans(Set.of())
            .notificationSettings(Set.of())
            .build();
        when(volunteerService.getOrCreate("other-user")).thenReturn(volunteer);
        when(notificationService.getNotificationsForUser("other-user")).thenReturn(new ArrayList<>());

        var result = userProfileService.getUserProfile("other-user");

        assertThat(result.getAccount().getVolunteer().getId()).isEqualTo("other-user");
        verify(userDirectoryService).getUserById("other-user");
    }
}

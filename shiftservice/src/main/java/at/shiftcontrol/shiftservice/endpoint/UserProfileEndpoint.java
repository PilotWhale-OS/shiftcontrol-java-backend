package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.NotificationSettingsUpdateDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/me")
@RequiredArgsConstructor
public class UserProfileEndpoint {
    private final ApplicationUserProvider userProvider;
    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    // TODO Security
    @Operation(
        operationId = "getCurrentUserProfile",
        description = "Get profile data of the current user (account, notifications, unavailability)"
    )
    public UserProfileDto getCurrentUserProfile() throws NotFoundException {
        return userProfileService.getUserProfile(userProvider.getCurrentUser().getUserId());
    }

    @PutMapping("/profile/notifications")
    // TODO Security
    @Operation(
        operationId = "updateNotificationSettings",
        description = "Update notification settings of the current user"
    )
    public NotificationSettingsDto updateNotificationSettings(@RequestBody NotificationSettingsUpdateDto updateDto) {
        return null; // TODO: implement
    }
}

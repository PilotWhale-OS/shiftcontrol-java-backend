package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.NotificationSettingsUpdateDto;
import at.shiftcontrol.shiftservice.dto.UnavailabilityCreateDto;
import at.shiftcontrol.shiftservice.dto.UnavailabilityDto;
import at.shiftcontrol.shiftservice.dto.UserProfileDto;
import at.shiftcontrol.shiftservice.service.UserProfileService;

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

    @GetMapping("/unavailability")
    // TODO Security
    @Operation(
        operationId = "getUnavailabilities",
        description = "Get unavailability periods of the current user"
    )
    public Collection<UnavailabilityDto> getUnavailabilities() {
        return null; // TODO: implement
    }

    @PostMapping("/unavailability")
    // TODO Security
    @Operation(
        operationId = "createUnavailability",
        description = "Create a new unavailability period for the current user"
    )
    public UnavailabilityDto createUnavailability(@RequestBody UnavailabilityCreateDto createDto) {
        return null; // TODO: implement
    }

    @DeleteMapping("/unavailability/{unavailabilityId}")
    // TODO Security
    @Operation(
        operationId = "deleteUnavailability",
        description = "Delete an existing unavailability period of the current user"
    )
    public void deleteUnavailability(@PathVariable String unavailabilityId) {
        // TODO: implement
    }
}

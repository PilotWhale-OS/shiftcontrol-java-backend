package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class UserProfileDto {
    @NotNull
    private AccountInfoDto account; // fetched from keycloak

    @NotNull
    private NotificationSettingsDto notifications;

    @NotNull
    private Collection<UnavailabilityDto> unavailabilityDates;

    private Collection<RoleDto> assignedRoles;
}

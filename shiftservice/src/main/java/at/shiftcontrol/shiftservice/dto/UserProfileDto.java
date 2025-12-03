package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

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

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
    private Collection<RoleDto> assignedRoles;

    //Todo: move to be dependent on event context
    //     @NotNull
    //     private Collection<UnavailabilityDto> unavailabilityDates;
}

package at.shiftcontrol.shiftservice.dto.userprofile;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.RoleDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    @NotNull
    private AccountInfoDto account; // fetched from keycloak

    @NotNull
    private NotificationSettingsDto notifications;

    private Collection<AbsenceDto> absences;

    private Collection<RoleDto> assignedRoles;

    //Todo: move to be dependent on event context
    //     @NotNull
    //     private Collection<UnavailabilityDto> unavailabilityDates;
}

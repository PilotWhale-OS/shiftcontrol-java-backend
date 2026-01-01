package at.shiftcontrol.shiftservice.dto.userprofile;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    @NotNull
    private AccountInfoDto account;

    @NotNull
    private Collection<NotificationSettingsDto> notifications;

    private Collection<RoleDto> assignedRoles;
}

package at.shiftcontrol.shiftservice.dto.user;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlanDto {
    @NotNull
    @Valid
    private VolunteerDto volunteer;

    @NotNull
    private String email;

    @NotNull
    private Boolean isLocked;

    @NotNull
    private Collection<RoleDto> roles;
}

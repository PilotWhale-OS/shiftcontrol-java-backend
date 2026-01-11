package at.shiftcontrol.shiftservice.dto.userprofile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.auth.UserType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoDto {
    @NotNull
    @Valid
    private VolunteerDto volunteer;

    @NotNull
    private String username;

    @NotNull
    private String email;

    @NotNull
    private UserType userType;
}

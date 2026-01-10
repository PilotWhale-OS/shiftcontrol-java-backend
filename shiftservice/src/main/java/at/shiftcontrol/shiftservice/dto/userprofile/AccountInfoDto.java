package at.shiftcontrol.shiftservice.dto.userprofile;

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
    private String id; // todo use volunteerdto
    @NotNull
    private String username;
    @NotNull
    private String fistName;
    @NotNull
    private String lastName;
    @NotNull
    private String email;
    @NotNull
    private UserType userType;
}

package at.shiftcontrol.shiftservice.dto.userinvite;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInviteCreateDto {
    @NotBlank
    @Email
    private String email;

    private String preferredUsername;

    private String firstName;

    private String lastName;

    private String displayName;

    private Instant expiresAt;

    @Builder.Default
    private Collection<String> roleIds = List.of();

    @Valid
    @Builder.Default
    private Collection<UserInviteShiftPlanAccessCreateDto> shiftPlanAccesses = List.of();
}

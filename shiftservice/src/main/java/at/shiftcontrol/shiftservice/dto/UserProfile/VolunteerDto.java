package at.shiftcontrol.shiftservice.dto.UserProfile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerDto {
    @NotNull
    private long id;
    @NotNull
    private String username;
    @NotNull
    private String email;
}

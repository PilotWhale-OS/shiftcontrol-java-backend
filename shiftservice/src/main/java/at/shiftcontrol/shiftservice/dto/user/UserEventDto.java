package at.shiftcontrol.shiftservice.dto.user;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventDto {
    @NotNull
    private String id;

    @NotNull
    private String username;

    @NotNull
    private String email;

    @NotNull
    private Collection<String> planningPlans;

    @NotNull
    private Collection<String> volunteeringPlans;
}

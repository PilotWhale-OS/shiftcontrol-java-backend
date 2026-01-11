package at.shiftcontrol.shiftservice.dto.rewardpoints;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerPointsDto {
    @NotNull
    private String volunteerId;

    @NotNull
    private String fistName;

    @NotNull
    private String lastName;

    @NotNull
    private String email;

    @NotNull
    @Min(0)
    private int rewardPoints;
}

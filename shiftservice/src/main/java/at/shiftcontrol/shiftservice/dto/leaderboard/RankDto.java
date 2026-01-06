package at.shiftcontrol.shiftservice.dto.leaderboard;

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
public class RankDto {
    @NotNull
    private int rank;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Min(0)
    private int hours;
}

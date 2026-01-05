package at.shiftcontrol.shiftservice.dto.leaderboard;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderBoardDto {
    @NotNull
    private int size;

    @NotNull
    @Valid
    private Collection<RankDto> ranks;
}

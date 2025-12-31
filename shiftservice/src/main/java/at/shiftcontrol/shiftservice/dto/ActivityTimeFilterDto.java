package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityTimeFilterDto {
    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @NotNull
    private Integer toleranceInMinutes;
}

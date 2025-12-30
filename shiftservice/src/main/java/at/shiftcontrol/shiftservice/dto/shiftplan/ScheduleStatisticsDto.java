package at.shiftcontrol.shiftservice.dto.shiftplan;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleStatisticsDto {
    @NotNull
    private int totalShifts;
    @NotNull
    private double totalHours;
    @NotNull
    private int unassignedCount;
}

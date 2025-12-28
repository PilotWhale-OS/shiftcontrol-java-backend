package at.shiftcontrol.shiftservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShiftPlanScheduleDaySearchDto extends ShiftPlanScheduleFilterDto {
    @NotNull
    private LocalDate date;
}

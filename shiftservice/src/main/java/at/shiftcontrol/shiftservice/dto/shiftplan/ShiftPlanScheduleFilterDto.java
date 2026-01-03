package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.type.ShiftRelevance;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ShiftPlanScheduleFilterDto {
    private String shiftName;
    @NotNull
    private Collection<ShiftRelevance> shiftRelevances;
    @NotNull
    private Collection<String> roleIds;
    @NotNull
    private Collection<String> locationIds;
}

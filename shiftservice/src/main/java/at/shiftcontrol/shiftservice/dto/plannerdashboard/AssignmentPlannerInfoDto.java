package at.shiftcontrol.shiftservice.dto.plannerdashboard;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentPlannerInfoDto {
    @NotNull
    private long shiftId;

    @NotNull
    private String shiftName;

    @NotNull
    @Valid
    private Collection<SlotAssignmentsDto> slots;
}

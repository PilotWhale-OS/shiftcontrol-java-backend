package at.shiftcontrol.shiftservice.dto.plannerdashboard;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotAssignmentsDto {

    @NotNull
    @Valid
    private Collection<AssignmentDto> assignments;

    @NotNull
    @Valid
    private String positionSlotName;
}

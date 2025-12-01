package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionSlotDto {
    @NotNull
    private String id;

    @NotNull
    private RoleDto role;

    private int count;
}

package at.shiftcontrol.shiftservice.dto.positionslot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.type.PositionSignupState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionSlotJoinErrorDto {
    private PositionSignupState state;
    private String message;
}

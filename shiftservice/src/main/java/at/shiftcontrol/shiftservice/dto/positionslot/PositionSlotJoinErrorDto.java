package at.shiftcontrol.shiftservice.dto.positionslot;

import at.shiftcontrol.shiftservice.type.PositionSignupState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionSlotJoinErrorDto {
    private PositionSignupState state;
    private String message;
}

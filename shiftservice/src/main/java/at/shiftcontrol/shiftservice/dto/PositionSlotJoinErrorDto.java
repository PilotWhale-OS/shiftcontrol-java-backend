package at.shiftcontrol.shiftservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.PositionSignupState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionSlotJoinErrorDto {
    private PositionSignupState state;
    private String message;
}

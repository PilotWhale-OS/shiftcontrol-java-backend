package at.shiftcontrol.shiftservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferenceUpdateDto {
    private int preferenceValue;
    // TODO additional fields like setting blockers, etc?
}

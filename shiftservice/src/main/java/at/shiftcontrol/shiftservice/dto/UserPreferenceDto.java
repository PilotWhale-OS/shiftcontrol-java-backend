package at.shiftcontrol.shiftservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferenceDto {
    private boolean hasBlocker;
    private int preferenceValue;
}

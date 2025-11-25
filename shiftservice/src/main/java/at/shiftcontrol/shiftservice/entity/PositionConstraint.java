package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Builder
@EqualsAndHashCode
public class PositionConstraint {
    @NonNull
    private PositionSlot position;

    //Todo: Add constraint details
}

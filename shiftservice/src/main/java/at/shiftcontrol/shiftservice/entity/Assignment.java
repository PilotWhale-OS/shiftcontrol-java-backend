package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Builder
@EqualsAndHashCode
public class Assignment {
    @NonNull
    private PositionSlot positionSlot;
    @NonNull
    private Volunteer volunteer;

    private AssignmentStatus status;

    @Nullable
    private AssignmentSwitchRequest switchRequest;
}

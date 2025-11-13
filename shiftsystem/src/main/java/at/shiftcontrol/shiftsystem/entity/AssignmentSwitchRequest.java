package at.shiftcontrol.shiftsystem.entity;

import at.shiftcontrol.shiftsystem.type.AssignmentSwitchRequestStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Builder
@EqualsAndHashCode
public class AssignmentSwitchRequest {
    private long id;

    @NonNull
    private Assignment requesterAssignment;
    @NonNull
    private Assignment requestedAssignment;
    @NonNull
    private AssignmentSwitchRequestStatus status;

    private String reason;
}

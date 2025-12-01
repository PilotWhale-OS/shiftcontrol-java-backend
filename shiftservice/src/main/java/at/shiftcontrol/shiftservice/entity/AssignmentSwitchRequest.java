package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.TradeStatus;
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
    private TradeStatus status;

    private String reason;
}

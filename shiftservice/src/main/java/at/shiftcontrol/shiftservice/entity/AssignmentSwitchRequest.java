package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.TradeStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.Instant;

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
    @NonNull
    private Instant createdAt;
}

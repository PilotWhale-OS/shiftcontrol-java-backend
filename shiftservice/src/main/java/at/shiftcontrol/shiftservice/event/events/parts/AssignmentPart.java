package at.shiftcontrol.shiftservice.event.events.parts;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@Data
@Builder
public class AssignmentPart {
    private String volunteerId;
    private AssignmentStatus status;

    private PositionSlotPart positionSlot;

    @NonNull
    public static AssignmentPart of(@NonNull Assignment assignment) {
        return AssignmentPart.builder()
            .volunteerId(assignment.getId().getVolunteerId())
            .status(assignment.getStatus())
            .positionSlot(PositionSlotPart.of(assignment.getPositionSlot()))
            .build();
    }
}

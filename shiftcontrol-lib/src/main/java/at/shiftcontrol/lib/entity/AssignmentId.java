package at.shiftcontrol.lib.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentId {
    // TODO NEEDED?
    private long positionSlotId;
    private String volunteerId;

    public static AssignmentId of(long positionSlotId, String volunteerId) {
        return AssignmentId.builder()
                .positionSlotId(positionSlotId)
                .volunteerId(volunteerId)
                .build();
    }

    public static AssignmentId of(Assignment assignment) {
        return AssignmentId.builder()
                .positionSlotId(assignment.getPositionSlot().getId())
                .volunteerId(assignment.getAssignedVolunteer().getId())
                .build();
    }
}

package at.shiftcontrol.lib.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSwitchRequestId {
    // TODO NEEDED?
    private AssignmentId offering;
    private AssignmentId requested;

    public static AssignmentSwitchRequestId of(AssignmentId offering, AssignmentId requested) {
        return AssignmentSwitchRequestId.builder()
                .offering(offering)
                .requested(requested)
                .build();
    }

    public static AssignmentSwitchRequestId of(Assignment offering, Assignment requested) {
        return new AssignmentSwitchRequestId(
            AssignmentId.of(offering),
            AssignmentId.of(requested)
        );
    }
}

package at.shiftcontrol.shiftservice.entity;

import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AssignmentSwitchRequestId implements Serializable {
    @AttributeOverrides({
        @AttributeOverride(name = "positionSlotId", column = @Column(name = "requester_position_slot_id")),
        @AttributeOverride(name = "volunteerId", column = @Column(name = "requester_volunteer_id"))
    })
    private AssignmentId offering;
    @AttributeOverrides({
        @AttributeOverride(name = "positionSlotId", column = @Column(name = "requested_position_slot_id")),
        @AttributeOverride(name = "volunteerId", column = @Column(name = "requested_volunteer_id"))
    })
    private AssignmentId requested;
}

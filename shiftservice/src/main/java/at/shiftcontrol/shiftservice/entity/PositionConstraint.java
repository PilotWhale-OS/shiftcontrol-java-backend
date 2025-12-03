package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "position_constraint")
public class PositionConstraint {

    @EmbeddedId
    private PositionConstraintId id;

    @NotNull
    @MapsId("sourcePositionSlotId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_position_slot_id", referencedColumnName = "id", nullable = false)
    private PositionSlot source;

    @NotNull
    @MapsId("targetPositionSlotId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "target_position_slot_id", referencedColumnName = "id", nullable = false)
    private PositionSlot target;

    // TODO: Add constraint details here
}

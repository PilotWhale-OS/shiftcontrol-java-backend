package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

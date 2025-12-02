package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class PositionConstraintId implements Serializable {
    private long sourcePositionSlotId;
    private long targetPositionSlotId;
}

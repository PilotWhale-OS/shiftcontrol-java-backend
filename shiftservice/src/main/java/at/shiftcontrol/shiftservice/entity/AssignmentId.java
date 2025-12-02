package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class AssignmentId implements Serializable {

    private long positionSlotId;
    private long volunteerId;
}

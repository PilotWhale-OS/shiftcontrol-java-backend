package at.shiftcontrol.shiftservice.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class AssignmentId implements Serializable {
    private long positionSlotId;
    private String volunteerId;

    public static AssignmentId of(long positionSlotId, String volunteerId) {
        return AssignmentId.builder()
                .positionSlotId(positionSlotId)
                .volunteerId(volunteerId)
                .build();
    }
}

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
public class PositionSlotPreferenceId implements Serializable {
    private String volunteerId;
    private String positionSlotId;

    public static PositionSlotPreferenceId of(String volunteerId, String positionSlotId) {
        return PositionSlotPreferenceId.builder()
                .volunteerId(volunteerId)
                .positionSlotId(positionSlotId)
                .build();
    }
}

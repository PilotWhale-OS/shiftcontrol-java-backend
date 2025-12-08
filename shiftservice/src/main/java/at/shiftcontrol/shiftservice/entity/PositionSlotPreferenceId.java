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
    private long volunteerId;
    private long positionSlotId;

    public static PositionSlotPreferenceId of(long volunteerId, long positionSlotId) {
        return PositionSlotPreferenceId.builder()
                .volunteerId(volunteerId)
                .positionSlotId(positionSlotId)
                .build();
    }
}

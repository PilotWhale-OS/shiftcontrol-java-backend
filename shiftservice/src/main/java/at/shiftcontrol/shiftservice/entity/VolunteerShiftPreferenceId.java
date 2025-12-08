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
public class VolunteerShiftPreferenceId implements Serializable {
    private String volunteerId;
    private String shiftId;

    public static VolunteerShiftPreferenceId of(String volunteerId, String shiftId) {
        return VolunteerShiftPreferenceId.builder()
                .volunteerId(volunteerId)
                .shiftId(shiftId)
                .build();
    }
}

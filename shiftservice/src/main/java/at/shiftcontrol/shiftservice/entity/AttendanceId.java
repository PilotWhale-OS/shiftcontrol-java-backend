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
public class AttendanceId implements Serializable {
    private long volunteerId;
    private long eventId;

    public static AttendanceId of(long volunteerId, long eventId) {
        return AttendanceId.builder()
                .volunteerId(volunteerId)
                .eventId(eventId)
                .build();
    }
}

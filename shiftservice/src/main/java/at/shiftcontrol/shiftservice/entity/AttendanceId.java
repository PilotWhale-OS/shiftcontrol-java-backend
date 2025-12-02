package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class AttendanceId implements Serializable {

    private long volunteerId;
    private long eventId;
}

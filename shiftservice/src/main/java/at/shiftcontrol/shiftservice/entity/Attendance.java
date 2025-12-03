package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.AttendanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendance")
public class Attendance {

    @EmbeddedId
    private AttendanceId id;

    @NotNull
    @MapsId("volunteerId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @NotNull
    @MapsId("eventId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
}

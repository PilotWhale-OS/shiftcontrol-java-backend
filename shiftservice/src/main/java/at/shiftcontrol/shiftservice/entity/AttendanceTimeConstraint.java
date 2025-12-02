package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendance_time_constraint")
public class AttendanceTimeConstraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "attendance_volunteer_id", referencedColumnName = "volunteer_id"),
            @JoinColumn(name = "attendance_event_id", referencedColumnName = "event_id")
    })
    private Attendance attendance;

    // startDate, endDate
}

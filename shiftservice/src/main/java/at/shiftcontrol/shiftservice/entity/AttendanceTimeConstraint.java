package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendance_time_constraint")
public class AttendanceTimeConstraint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumns({
        @JoinColumn(name = "attendance_volunteer_id", referencedColumnName = "volunteer_id"),
        @JoinColumn(name = "attendance_event_id", referencedColumnName = "event_id")
    })
    private Attendance attendance;

    @Override
    public String toString() {
        return "AttendanceTimeConstraint{" +
            "id=" + id +
            ", attendance=" + attendance +
            '}';
    }
    // startDate, endDate
}

package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Builder
@EqualsAndHashCode
public class AttendanceTimeConstraint {
    @NonNull
    private Attendance attendance;

    // startDate, endDate
}

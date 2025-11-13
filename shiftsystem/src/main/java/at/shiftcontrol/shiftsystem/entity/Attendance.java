package at.shiftcontrol.shiftsystem.entity;

import at.shiftcontrol.shiftsystem.type.AttendanceStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Builder
@EqualsAndHashCode
public class Attendance {
    @NonNull
    private Volunteer volunteer;
    @NonNull
    private Event event;

    // plannedArrivalDate, arrivalDate, departureDate
    @NonNull
    private AttendanceStatus status;
}

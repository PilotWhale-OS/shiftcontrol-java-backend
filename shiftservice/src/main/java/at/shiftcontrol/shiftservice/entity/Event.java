package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

@Data
@Builder
@EqualsAndHashCode
public class Event {
    private long id;

    private String name;

    //StartDate, endDate

    Collection<Location> locations;
    Collection<Attendance> attendances;
    Collection<ShiftPlan> shiftPlans;
}

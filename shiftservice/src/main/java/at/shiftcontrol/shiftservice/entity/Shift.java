package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.LockStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Collection;

@Data
@Builder
@EqualsAndHashCode
public class Shift {
    private long id;
    @NonNull
    private ShiftPlan shiftPlan;

    private String name;
    private String description;

    // startDate, endDate

    private LockStatus lockStatus;

    private Collection<Location> locations;
    private Collection<Activity> relatedActivities;
    private Collection<PositionSlot> slots;
}

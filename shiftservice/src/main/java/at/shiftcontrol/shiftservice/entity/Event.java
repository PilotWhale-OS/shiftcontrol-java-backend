package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Collection;

@Data
@Builder
@EqualsAndHashCode
public class Event {
    private long id;

    private String name;
    private String shortDescription;
    private String longDescription;
    private Instant startTime;
    private Instant endTime;

    Collection<Location> locations;
    Collection<Attendance> attendances; // TODO only relevant for magament view (not for volunteer) --> not included in volunteer DTOs
    Collection<ShiftPlan> shiftPlans;
}

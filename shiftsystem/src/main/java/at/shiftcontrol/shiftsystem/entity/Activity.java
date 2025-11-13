package at.shiftcontrol.shiftsystem.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Collection;

@Data
@Builder
@EqualsAndHashCode
public class Activity {
    private long id;
    @NonNull
    private Event event;

    private String name;
    private String description;

    private Collection<Shift> shifts;
    private Location location;
}

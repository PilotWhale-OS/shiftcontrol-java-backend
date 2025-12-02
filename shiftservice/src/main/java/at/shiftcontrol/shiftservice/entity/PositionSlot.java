package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Collection;

@Data
@Builder
@EqualsAndHashCode
public class PositionSlot {
    private long id;

    @NonNull
    private Shift shift;

    @NonNull
    private Role role;
    
    private Collection<Volunteer> assignedVolunteers;

    @NonNull
    private Location location;

    private int desiredVolunteerCount;
}

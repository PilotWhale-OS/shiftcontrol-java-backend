package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Builder
@EqualsAndHashCode
public class PositionSlot {
    @NonNull
    private Shift shift;
    private long id;

    @NonNull
    private Role role;
    private int count;
}

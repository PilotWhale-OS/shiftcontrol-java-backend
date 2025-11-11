package at.shiftcontrol.shiftsystem.entity;

import at.shiftcontrol.shiftsystem.type.LockStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class Shift {
    private Long id;

    private String name;
    private String description;

    // startDate, endDate

    private LockStatus lockStatus;
}

package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class Role {
    private long id;
    private String name;
    private String description;

    //Todo: openForSelfAssignment indicates whether users can assign this role to themselves
}

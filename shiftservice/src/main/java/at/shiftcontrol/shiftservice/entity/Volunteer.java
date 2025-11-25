package at.shiftcontrol.shiftservice.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class Volunteer {
    private long id;

    private String name;
    private String email;
}

package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityDto {
    @NotNull
    private long id;
    @NotNull
    private String name;
    private String description;
    private LocationDto location;
}

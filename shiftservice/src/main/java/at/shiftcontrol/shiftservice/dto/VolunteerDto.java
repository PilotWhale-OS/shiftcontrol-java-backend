package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VolunteerDto {
    @NotNull
    private String id;

    @NotNull
    private String name;

    @NotNull
    private String email;
}

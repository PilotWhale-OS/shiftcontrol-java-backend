package at.shiftcontrol.shiftservice.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationModificationDto {
    @NotNull
    private String name;

    private String description;
    private String url;
}

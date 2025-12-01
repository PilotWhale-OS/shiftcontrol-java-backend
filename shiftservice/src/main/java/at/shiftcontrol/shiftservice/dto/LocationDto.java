package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationDto {
    @NotNull
    private long id;

    @NotNull
    private String name;
    
    private String description;
    private String url;
}

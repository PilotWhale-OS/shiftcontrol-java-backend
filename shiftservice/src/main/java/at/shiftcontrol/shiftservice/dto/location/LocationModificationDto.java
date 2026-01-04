package at.shiftcontrol.shiftservice.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class LocationModificationDto {
    @NotNull
    private String name;

    private String description;
    private String url;
}

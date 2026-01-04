package at.shiftcontrol.shiftservice.dto.location;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class LocationModificationDto {
    @NotNull
    @Size(max = 50)
    private String name;

    @Size(max = 255)
    private String description;

    private String url;
}

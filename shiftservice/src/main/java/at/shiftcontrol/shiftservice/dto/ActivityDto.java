package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityDto {
    @NotNull
    private String id;
    @NotNull
    private String name;
    private String description;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
    private LocationDto location;
}

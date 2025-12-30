package at.shiftcontrol.shiftservice.dto.shift;

import java.time.Instant;

import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.dto.LocationDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftModificationDto {
    @NotNull
    private String name;
    private String shortDescription;
    private String longDescription;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;

    private ActivityDto relatedActivity;
    private LocationDto location;
}

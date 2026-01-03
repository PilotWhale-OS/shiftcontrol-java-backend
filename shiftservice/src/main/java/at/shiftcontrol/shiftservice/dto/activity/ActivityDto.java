package at.shiftcontrol.shiftservice.dto.activity;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.location.LocationDto;

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
    @NotNull
    private boolean readOnly;
}

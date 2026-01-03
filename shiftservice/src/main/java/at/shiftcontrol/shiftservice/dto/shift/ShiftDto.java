package at.shiftcontrol.shiftservice.dto.shift;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftDto {
    @NotNull
    private String id;
    @NotNull
    private String name;
    private String shortDescription;
    private String longDescription;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
    private ActivityDto relatedActivity;
    @NotNull
    private Collection<PositionSlotDto> positionSlots;
    @NotNull
    private LocationDto location;
}

package at.shiftcontrol.shiftservice.dto.shift;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.dto.LocationDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.type.LockStatus;
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
    @NotNull
    private Collection<ActivityDto> relatedActivities;
    @NotNull
    private Collection<PositionSlotDto> positionSlots;
    @NotNull
    private LockStatus lockStatus;
    @NotNull
    private LocationDto location;
}

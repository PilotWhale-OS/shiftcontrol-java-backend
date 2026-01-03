package at.shiftcontrol.shiftservice.dto.shift;

import java.time.Instant;
import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;

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

    @Valid
    private ActivityDto relatedActivity;

    @NotNull
    @Valid
    private Collection<PositionSlotDto> positionSlots;

    @NotNull
    @Valid
    private LocationDto location;
}

package at.shiftcontrol.shiftservice.dto.event;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleDto {
    @NotNull
    private EventDto event;
    @NotNull
    private Collection<ActivityDto> activities;
}

package at.shiftcontrol.shiftservice.dto.event;

import java.util.Collection;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;

@Data
@Builder
public class EventScheduleDto {
    @Valid
    private EventDto event;

    @Valid
    private Collection<ActivityDto> activities;
}

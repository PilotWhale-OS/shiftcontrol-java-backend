package at.shiftcontrol.shiftservice.dto.event;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.ActivityDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleDto {
    private EventDto event;
    private Collection<ActivityDto> activities;
}

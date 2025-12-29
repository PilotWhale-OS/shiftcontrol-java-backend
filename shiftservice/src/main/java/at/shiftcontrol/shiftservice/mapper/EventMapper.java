package at.shiftcontrol.shiftservice.mapper;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.entity.Event;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EventMapper {
    public static EventDto toEventOverviewDto(Event event) {
        return EventDto.builder()
            .id(String.valueOf(event.getId()))
            .name(event.getName())
            .longDescription(event.getLongDescription())
            .shortDescription(event.getShortDescription())
            .startTime(event.getStartTime())
            .endTime(event.getEndTime())
            .build();
    }

    public static List<EventDto> toEventOverviewDto(List<Event> events) {
        return events.stream()
            .map(EventMapper::toEventOverviewDto)
            .toList();
    }
}

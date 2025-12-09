package at.shiftcontrol.shiftservice.mapper;

import java.util.List;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.entity.Event;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EventMapper {
    public static EventDto toEventOverviewDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
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

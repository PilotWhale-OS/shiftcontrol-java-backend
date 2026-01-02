package at.shiftcontrol.shiftservice.mapper;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDto;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.entity.Event;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EventMapper {
    public static EventDto toEventDto(Event event) {
        return EventDto.builder()
            .id(String.valueOf(event.getId()))
            .name(event.getName())
            .longDescription(event.getLongDescription())
            .shortDescription(event.getShortDescription())
            .startTime(event.getStartTime())
            .endTime(event.getEndTime())
            .build();
    }

    public static List<EventDto> toEventDto(List<Event> events) {
        return events.stream()
            .map(EventMapper::toEventDto)
            .toList();
    }

    public static Event toEvent(EventModificationDto modificationDto) {
        return Event.builder()
            .name(modificationDto.getName())
            .shortDescription(modificationDto.getShortDescription())
            .longDescription(modificationDto.getLongDescription())
            .startTime(modificationDto.getStartTime())
            .endTime(modificationDto.getEndTime())
            .build();
    }

    public static EventScheduleDto toEventScheduleDto(Event event, List<Activity> activities) {
        return EventScheduleDto.builder()
            .event(toEventDto(event))
            .activities(ActivityMapper.toActivityDto(activities))
            .build();
    }
}

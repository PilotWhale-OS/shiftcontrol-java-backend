package at.shiftcontrol.shiftservice.mapper;

import java.util.List;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
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

    public static void updateEvent(Event event, EventModificationDto eventModificationDto) {
        event.setName(eventModificationDto.getName());
        event.setShortDescription(eventModificationDto.getShortDescription());
        event.setLongDescription(eventModificationDto.getLongDescription());
        event.setStartTime(eventModificationDto.getStartTime());
        event.setEndTime(eventModificationDto.getEndTime());
    }
}

package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsInternalDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EventPointsDtoMapper {
    public static EventPointsDto toEventPointsDto(EventPointsInternalDto eventPointsInternalDto) {
        return new EventPointsDto(String.valueOf(eventPointsInternalDto.eventId()), (int) eventPointsInternalDto.points());
    }

    public static Collection<EventPointsDto> toEventPointsDto(Collection<EventPointsInternalDto> eventPointsInternalDtos) {
        return eventPointsInternalDtos.stream()
            .map(EventPointsDtoMapper::toEventPointsDto)
            .toList();
    }
}

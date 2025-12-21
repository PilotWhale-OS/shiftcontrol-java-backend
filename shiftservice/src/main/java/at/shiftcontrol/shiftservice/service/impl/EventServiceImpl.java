package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EventService;
import at.shiftcontrol.shiftservice.service.StatisticService;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final StatisticService statisticService;

    @Override
    public List<EventDto> search(EventSearchDto searchDto) {
        return EventMapper.toEventOverviewDto(eventDao.search(searchDto));
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var shiftPlans = event.getShiftPlans();

        var eventOverviewDto = EventMapper.toEventOverviewDto(event);

        // TODO only show shift plans that are relevant to the user (e.g. public shift plans or ones the user is assigned to (based on roles/permissions))
        var shiftPlanDtos = shiftPlans.stream()
            .map(ShiftPlanMapper::toShiftPlanDto)
            .toList();

        //Todo: implement reward points
        return EventShiftPlansOverviewDto.builder()
            .eventOverview(eventOverviewDto)
            .shiftPlans(shiftPlanDtos)
            .rewardPoints(-1)
            .ownEventStatistics(statisticService.getOwnEventStatistics(eventId, userId))
            .overallEventStatistics(statisticService.getOverallEventStatistics(eventId))
            .build();
    }
}

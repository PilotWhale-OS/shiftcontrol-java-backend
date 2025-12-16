package at.shiftcontrol.shiftservice.service.impl;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.ShiftPlanEventService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftPlanEventServiceImpl implements ShiftPlanEventService {
    private final ShiftPlanDao shiftPlanDao;
    private final EventDao eventDao;
    private final StatisticService statisticService;

    @Override
    public EventShiftPlansOverviewDto getShiftPlansOverview(long eventId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var shiftPlans = shiftPlanDao.findByEventId(eventId);

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
            .ownEventStatistics(statisticService.getOwnEventStatistics(eventId, -1)) // Todo: pass user id
            .overallEventStatistics(statisticService.getOverallEventStatistics(eventId))
            .build();
    }
}

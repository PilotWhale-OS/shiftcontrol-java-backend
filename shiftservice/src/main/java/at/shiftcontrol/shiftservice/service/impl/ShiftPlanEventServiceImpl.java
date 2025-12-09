package at.shiftcontrol.shiftservice.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.ShiftPlanEventService;

@Service
@RequiredArgsConstructor
public class ShiftPlanEventServiceImpl implements ShiftPlanEventService {
    private final ShiftPlanDao shiftPlanDao;
    private final EventDao eventDao;

    @Override
    public EventShiftPlansOverviewDto getShiftPlansOverview(long eventId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var shiftPlans = shiftPlanDao.findByEventId(eventId);

        var eventOverviewDto = EventMapper.toEventOverviewDto(event);
        var shiftPlanDtos = shiftPlans.stream()
                .map(ShiftPlanMapper::shiftPlanToShiftPlanDto)
                .toList();

        //Todo: implement reward points and statistics
        return EventShiftPlansOverviewDto.builder()
            .eventOverview(eventOverviewDto)
            .shiftPlans(shiftPlanDtos)
            .rewardPoints(-1)
            .ownShiftPlanStatistics(null)
            .overallShiftPlanStatistics(null)
            .build();
    }
}

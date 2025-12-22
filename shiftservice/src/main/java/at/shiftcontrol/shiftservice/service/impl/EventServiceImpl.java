package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.dto.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EventService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final VolunteerDao volunteerDao;
    private final StatisticService statisticService;

    @Override
    public List<EventDto> search(EventSearchDto searchDto) {
        // TODO filter here based on events which are relevant for the user (e.g. where the user is a volunteer)
        return EventMapper.toEventOverviewDto(eventDao.search(searchDto));
    }

    @Override
    public List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var shiftPlans = event.getShiftPlans();
        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with id: " + userId));

        // TODO only show shift plans that are relevant to the user (e.g. volunteeringPlans of volunteer)
//        var volunteerShiftPlans = volunteer.getVolunteeringPlans();

        // TODO dummy values for now; CHANGE TO REAL DATA like above once other MR is merged
        var volunteerShiftPlans = shiftPlans;

        // now filter shiftPlans that the volunteer is part of (volunteerShiftPlans)
        var relevantShiftPlans = shiftPlans.stream()
            .filter(volunteerShiftPlans::contains)
            .toList();

        return ShiftPlanMapper.toShiftPlanDto(relevantShiftPlans);
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        var eventOverviewDto = EventMapper.toEventOverviewDto(event);

        //Todo: implement reward points
        return EventShiftPlansOverviewDto.builder()
            .eventOverview(eventOverviewDto)
            .shiftPlans(getUserRelatedShiftPlansOfEvent(eventId, userId))
            .rewardPoints(-1)
            .ownEventStatistics(statisticService.getOwnEventStatistics(eventId, userId))
            .overallEventStatistics(statisticService.getOverallEventStatistics(eventId))
            .build();
    }
}

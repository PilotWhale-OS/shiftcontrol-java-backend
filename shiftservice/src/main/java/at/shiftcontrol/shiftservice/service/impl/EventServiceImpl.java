package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EventService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final ActivityDao activityDao;
    private final VolunteerDao volunteerDao;
    private final StatisticService statisticService;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;

    @Override
    public EventDto getEvent(long eventId) throws NotFoundException, ForbiddenException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        securityHelper.assertUserIsinAnyPlanOfEvent(event);

        return EventMapper.toEventDto(event);
    }

    @Override
    public List<EventDto> search(EventSearchDto searchDto) throws NotFoundException {
        var filteredEvents = eventDao.search(searchDto);
        var currentUser = userProvider.getCurrentUser();

        // skip filtering for admin users
        if (currentUser instanceof AdminUser) {
            return EventMapper.toEventDto(filteredEvents);
        }
        String userId = currentUser.getUserId();

        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with id: " + userId));
        var volunteerShiftPlans = volunteer.getVolunteeringPlans();
        var planningShiftPlans = volunteer.getPlanningPlans();

        // filter events that the volunteer is part of
        var relevantEvents = filteredEvents.stream()
            .filter(event -> event.getShiftPlans().stream()
                .anyMatch(shiftPlan -> volunteerShiftPlans.contains(shiftPlan) || planningShiftPlans.contains(shiftPlan))
            )
            .toList();

        return EventMapper.toEventDto(relevantEvents);
    }

    @Override
    public List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) throws NotFoundException {
        return ShiftPlanMapper.toShiftPlanDto(getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId));
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        var eventOverviewDto = EventMapper.toEventDto(event);
        var userRelevantShiftPlans = getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId);

        //Todo: implement reward points
        return EventShiftPlansOverviewDto.builder()
            .eventOverview(eventOverviewDto)
            .shiftPlans(ShiftPlanMapper.toShiftPlanDto(userRelevantShiftPlans))
            .rewardPoints(-1)
            .ownEventStatistics(statisticService.getOwnStatisticsOfShiftPlans(userRelevantShiftPlans, userId))
            .overallEventStatistics(statisticService.getOverallEventStatistics(event))
            .build();
    }

    @Override
    public EventScheduleDto getEventSchedule(long eventId, EventScheduleDaySearchDto searchDto) throws NotFoundException, ForbiddenException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        securityHelper.assertUserIsinAnyPlanOfEvent(event);

        var activitiesOfEvent = activityDao.searchActivitiesInEvent(eventId, searchDto).stream().toList();

        return EventMapper.toEventScheduleDto(event, activitiesOfEvent);
    }

    @Override
    public EventDto createEvent(@NonNull EventModificationDto modificationDto) {
        // TODO assert admin only

        validateEventModificationDto(modificationDto);

        Event event = EventMapper.toEvent(modificationDto);
        event = eventDao.save(event);
        return EventMapper.toEventDto(event);
    }

    @Override
    public EventDto updateEvent(long eventId, @NonNull EventModificationDto modificationDto) throws NotFoundException {
        // TODO assert admin only

        validateEventModificationDto(modificationDto);

        Event event = eventDao.findById(eventId).orElseThrow(NotFoundException::new);
        EventMapper.updateEvent(event, modificationDto);
        eventDao.save(event);
        return EventMapper.toEventDto(event);
    }

    private void validateEventModificationDto(EventModificationDto modificationDto) {
        if (modificationDto.getStartTime().isAfter(modificationDto.getEndTime())) {
            throw new BadRequestException("Event end time must be after start time");
        }
    }

    @Override
    public void deleteEvent(long eventId) throws NotFoundException {
        // TODO assert admin only
        eventDao.delete(eventDao.findById(eventId).orElseThrow(NotFoundException::new));
    }

    private List<ShiftPlan> getUserRelatedShiftPlanEntitiesOfEvent(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var shiftPlans = event.getShiftPlans();

        // skip filtering for admin users
        var currentUser = userProvider.getCurrentUser();
        if (currentUser instanceof AdminUser) {
            return shiftPlans.stream().toList();
        }

        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with id: " + userId));

        var volunteerShiftPlans = volunteer.getVolunteeringPlans();
        var planningShiftPlans = volunteer.getPlanningPlans();

        // filter shiftPlans that the volunteer is part of (volunteerShiftPlans)
        return shiftPlans.stream()
            .filter(shiftPlan -> volunteerShiftPlans.contains(shiftPlan) || planningShiftPlans.contains(shiftPlan))
            .toList();
    }
}

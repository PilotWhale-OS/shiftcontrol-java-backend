package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;
import java.util.Map;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.EventEvent;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EventService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static at.shiftcontrol.shiftservice.event.RoutingKeys.EVENT_CLONED;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final LocationDao locationDao;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftDao shiftDao;
    private final RoleDao roleDao;
    private final ActivityDao activityDao;
    private final VolunteerDao volunteerDao;
    private final StatisticService statisticService;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    @Override
    public EventDto getEvent(long eventId) {
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        return EventMapper.toEventDto(event);
    }

    @Override
    public List<EventDto> search(EventSearchDto searchDto) {
        var filteredEvents = eventDao.search(searchDto);
        var currentUser = userProvider.getCurrentUser();

        // skip filtering for admin users
        if (securityHelper.isUserAdmin(currentUser)) {
            return EventMapper.toEventDto(filteredEvents);
        }
        String userId = currentUser.getUserId();

        var volunteer = volunteerDao.getById(userId);
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
    public List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) {
        return ShiftPlanMapper.toShiftPlanDto(getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId));
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) {
        var event = eventDao.getById(eventId);

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
    public EventScheduleDto getEventSchedule(long eventId, EventScheduleDaySearchDto searchDto) {
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        var activitiesOfEvent = activityDao.searchActivitiesInEvent(eventId, searchDto).stream().toList();

        return EventMapper.toEventScheduleDto(event, activitiesOfEvent);
    }

    @Override
    @AdminOnly
    public EventDto createEvent(@NonNull EventModificationDto modificationDto) {
        validateEventModificationDto(modificationDto);

        Event event = EventMapper.toEvent(modificationDto);
        event = eventDao.save(event);

        publisher.publishEvent(EventEvent.of(RoutingKeys.EVENT_CREATED, event));
        return EventMapper.toEventDto(event);
    }

    @Override
    @AdminOnly
    public EventDto updateEvent(long eventId, @NonNull EventModificationDto modificationDto) {
        validateEventModificationDto(modificationDto);

        Event event = eventDao.getById(eventId);
        EventMapper.updateEvent(event, modificationDto);
        eventDao.save(event);

        publisher.publishEvent(EventEvent.of(RoutingKeys.format(RoutingKeys.EVENT_UPDATED, Map.of("eventId", String.valueOf(eventId))), event));
        return EventMapper.toEventDto(event);
    }

    private void validateEventModificationDto(EventModificationDto modificationDto) {
        if (modificationDto.getStartTime().isAfter(modificationDto.getEndTime())) {
            throw new BadRequestException("Event end time must be after start time");
        }
    }

    @Override
    @AdminOnly
    public void deleteEvent(long eventId) {
        var event = eventDao.getById(eventId);

        publisher.publishEvent(EventEvent.of(RoutingKeys.format(RoutingKeys.EVENT_DELETED, Map.of("eventId", String.valueOf(eventId))), event));
        eventDao.delete(event);
    }

    private List<ShiftPlan> getUserRelatedShiftPlanEntitiesOfEvent(long eventId, String userId) {
        var event = eventDao.getById(eventId);
        var shiftPlans = event.getShiftPlans();

        // skip filtering for admin users
        if (securityHelper.isUserAdmin()) {
            return shiftPlans.stream().toList();
        }

        var volunteer = volunteerDao.getById(userId);

        var volunteerShiftPlans = volunteer.getVolunteeringPlans();
        var planningShiftPlans = volunteer.getPlanningPlans();

        // filter shiftPlans that the volunteer is part of (volunteerShiftPlans)
        return shiftPlans.stream()
            .filter(shiftPlan -> volunteerShiftPlans.contains(shiftPlan) || planningShiftPlans.contains(shiftPlan))
            .toList();
    }

    @Override
    @AdminOnly
    public EventDto cloneEvent(long eventId) {
        var sourceEvent = eventDao.getById(eventId);
        var targetEvent = cloneBasicEventFields(sourceEvent);

        cloneLocations(sourceEvent, targetEvent);
        // TODO set on both sides?

        // TODO clone all activities of the event
        // TODO clone all roles of the event
        // TODO clone all positions of the event

        cloneShiftPlan(sourceEvent, targetEvent);


        targetEvent = eventDao.save(targetEvent);
        publisher.publishEvent(EventEvent.of(RoutingKeys.format(EVENT_CLONED, Map.of(
                "sourceEventId", String.valueOf(sourceEvent.getId()),
                "newEventId", String.valueOf(targetEvent.getId()))),
            targetEvent));
        return EventMapper.toEventDto(targetEvent);
    }

    private Event cloneBasicEventFields(Event event) {
        var basicEvent = Event.builder()
            .name(event.getName() + " (Clone)")
            .shortDescription(event.getShortDescription())
            .longDescription(event.getLongDescription())
            .startTime(event.getStartTime())
            .endTime(event.getEndTime())
            .build();

        return eventDao.save(basicEvent);
    }

    private void cloneLocations(Event sourceEvent, Event targetEvent) {
        var locations = sourceEvent.getLocations().stream().map(location -> Location.builder()
            .event(targetEvent)
            .name(location.getName())
            .description(location.getDescription())
            .url(location.getUrl())
            .additionalProperties(location.getAdditionalProperties())
            .readOnly(location.isReadOnly())
            .build()).toList();

        locationDao.saveAll(locations);
    }

    private void cloneShiftPlan(Event sourceEvent, Event targetEvent) {
        var targetShiftPlans = sourceEvent.getShiftPlans().stream().map(shiftPlan -> ShiftPlan.builder()
            .event(targetEvent)
            .name(shiftPlan.getName())
            .shortDescription(shiftPlan.getShortDescription())
            .longDescription(shiftPlan.getLongDescription())
            .lockStatus(shiftPlan.getLockStatus())
            .defaultNoRolePointsPerMinute(shiftPlan.getDefaultNoRolePointsPerMinute())
            .build()).toList();

        targetShiftPlans = shiftPlanDao.saveAll(targetShiftPlans).stream().toList();

        var sourceShiftPlans = sourceEvent.getShiftPlans().stream().toList();
        addVolunteersToShiftPlan(sourceShiftPlans, targetShiftPlans);

        copyShifts(sourceShiftPlans, targetShiftPlans);
    }

    private void addVolunteersToShiftPlan(List<ShiftPlan> sourceShiftPlans, List<ShiftPlan> targetShiftPlans) {
        // add plan volunteers and planners
        for (int i = 0; i < targetShiftPlans.size(); i++) {
            var sourceShiftPlan = sourceShiftPlans.get(i);
            var targetShiftPlan = targetShiftPlans.get(i);
            for (var volunteer : sourceShiftPlan.getPlanVolunteers()) {
                targetShiftPlan.addPlanVolunteer(volunteer);
            }
            for (var planner : sourceShiftPlan.getPlanPlanners()) {
                targetShiftPlan.addPlanPlanner(planner);
            }
            volunteerDao.saveAll(targetShiftPlan.getPlanVolunteers());
            volunteerDao.saveAll(targetShiftPlan.getPlanPlanners());
        }
    }

    private void copyShifts(List<ShiftPlan> sourceShiftPlans, List<ShiftPlan> targetShiftPlans) {
        for (int i = 0; i < sourceShiftPlans.size(); i++) {
            var sourceShiftPlan = sourceShiftPlans.get(i);
            var targetShiftPlan = targetShiftPlans.get(i);

            var shifts = sourceShiftPlan.getShifts().stream().map(shift -> Shift.builder()
                .shiftPlan(targetShiftPlan)
                .name(shift.getName())
                .shortDescription(shift.getShortDescription())
                .longDescription(shift.getLongDescription())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                // TODO use cloned locations
                // TODO use cloned activities
                // TODO use clones positions slots
                .bonusRewardPoints(shift.getBonusRewardPoints())
                .build()).toList();
            shiftDao.saveAll(shifts);
        }
    }
}

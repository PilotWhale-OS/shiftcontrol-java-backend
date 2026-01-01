package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.dto.ActivitySuggestionDto;
import at.shiftcontrol.shiftservice.dto.ActivityTimeFilterDto;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.EventService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final ActivityDao activityDao;
    private final VolunteerDao volunteerDao;
    private final StatisticService statisticService;

    @Override
    public List<EventDto> search(EventSearchDto searchDto, String userId) throws NotFoundException {
        var filteredEvents = eventDao.search(searchDto);
        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with id: " + userId));
        var volunteerShiftPlans = volunteer.getVolunteeringPlans();

        // filter events that the volunteer is part of
        var relevantEvents = filteredEvents.stream()
            .filter(event -> event.getShiftPlans().stream()
                .anyMatch(volunteerShiftPlans::contains))
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
    public EventDto createEvent(EventModificationDto modificationDto) {
        Event event = EventMapper.toEvent(modificationDto);
        event = eventDao.save(event);
        return EventMapper.toEventDto(event);
    }

    @Override
    public EventDto updateEvent(long eventId, EventModificationDto eventModificationDto) throws NotFoundException {
        Event event = eventDao.findById(eventId).orElseThrow(NotFoundException::new);
        EventMapper.updateEvent(event, eventModificationDto);
        eventDao.save(event);
        return EventMapper.toEventDto(event);
    }

    @Override
    public void deleteEvent(long eventId) throws NotFoundException {
        eventDao.delete(eventDao.findById(eventId).orElseThrow(NotFoundException::new));
    }

    private List<ShiftPlan> getUserRelatedShiftPlanEntitiesOfEvent(long eventId, String userId) throws NotFoundException {
        var event = eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        var shiftPlans = event.getShiftPlans();
        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found with id: " + userId));

        var volunteerShiftPlans = volunteer.getVolunteeringPlans();

        // filter shiftPlans that the volunteer is part of (volunteerShiftPlans)
        return shiftPlans.stream()
            .filter(volunteerShiftPlans::contains)
            .toList();
    }

    @Override
    public Collection<ActivityDto> suggestActivitiesForShift(long eventId, ActivitySuggestionDto suggestionDto) throws NotFoundException {
        eventDao.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        var activitiesOfEvent = activityDao.findAllByEventId(eventId);

        if (suggestionDto == null || (suggestionDto.getName() == null && suggestionDto.getTimeFilter() == null)) {
            return activitiesOfEvent.stream()
                .map(ActivityMapper::toActivityDto)
                .toList();
        }

        var filteredStream = getActivityStream(suggestionDto, activitiesOfEvent);

        return filteredStream
            .map(ActivityMapper::toActivityDto)
            .toList();
    }

    private @NonNull Stream<Activity> getActivityStream(ActivitySuggestionDto suggestionDto, Collection<Activity> activitiesOfEvent) {
        var timeFilter = suggestionDto.getTimeFilter();
        Instant shiftStart = timeFilter != null ? timeFilter.getStartTime() : null;
        Instant shiftEnd = timeFilter != null ? timeFilter.getEndTime() : null;
        var filteredByTime = filterByTime(activitiesOfEvent, timeFilter, shiftStart, shiftEnd);

        return filterByName(filteredByTime, suggestionDto.getName());
    }

    private @NonNull Stream<Activity> filterByTime(Collection<Activity> activitiesOfEvent, ActivityTimeFilterDto timeFilter, Instant shiftStart,
                                                   Instant shiftEnd) {
        int toleranceMinutes = timeFilter != null ? timeFilter.getToleranceInMinutes() : 0;

        boolean applyTimeFilter = shiftStart != null && shiftEnd != null;

        if (applyTimeFilter && shiftEnd.isBefore(shiftStart)) {
            throw new BadRequestException("End time must be after start time in time filter");
        }

        if (!applyTimeFilter) {
            return activitiesOfEvent.stream();
        }

        // narrow down by start/end time
        // any intersection of suggestion times & activity times is valid
        return activitiesOfEvent.stream()
            .filter(activity -> {
                Instant activityStart = activity.getStartTime();
                Instant activityEnd = activity.getEndTime();
                return !(activityEnd.isBefore(shiftStart.minusSeconds(toleranceMinutes * 60L)) ||
                    activityStart.isAfter(shiftEnd.plusSeconds(toleranceMinutes * 60L)));
            });
    }

    private @NonNull Stream<Activity> filterByName(@NonNull Stream<Activity> filteredByTime, String name) {
        Stream<Activity> finalFiltered = filteredByTime;
        if (StringUtils.isNotBlank(name)) {
            String nameFilterLower = name.toLowerCase();
            finalFiltered = filteredByTime.filter(activity ->
                activity.getName() != null && activity.getName().toLowerCase().contains(nameFilterLower)
            );
        }
        return finalFiltered;
    }
}

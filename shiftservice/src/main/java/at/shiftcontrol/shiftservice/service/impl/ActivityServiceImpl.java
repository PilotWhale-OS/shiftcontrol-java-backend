package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.dto.ActivitySuggestionDto;
import at.shiftcontrol.shiftservice.dto.ActivityTimeFilterDto;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.service.ActivityService;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final EventDao eventDao;
    private final ActivityDao activityDao;
    private final VolunteerDao volunteerDao;
    private final StatisticService statisticService;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;

    @Override
    public Collection<ActivityDto> suggestActivitiesForShift(long eventId, ActivitySuggestionDto suggestionDto) throws NotFoundException {
        // TODO assert admin only

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

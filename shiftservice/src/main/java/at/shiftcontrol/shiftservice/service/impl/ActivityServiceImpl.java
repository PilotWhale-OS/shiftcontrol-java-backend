package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivityModificationDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivitySuggestionDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivityTimeFilterDto;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.service.ActivityService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final EventDao eventDao;
    private final ActivityDao activityDao;
    private final LocationDao locationDao;

    @Override
    public ActivityDto getActivity(long activityId) throws NotFoundException {
        var activity = activityDao.findById(activityId)
            .orElseThrow(() -> new NotFoundException("Activity not found with id: " + activityId));

        return ActivityMapper.toActivityDto(activity);
    }

    @Override
    public ActivityDto createActivity(long eventId, @NonNull ActivityModificationDto modificationDto) throws NotFoundException {
        // TODO assert admin only

        var event = eventDao.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        var newActivity = Activity.builder()
            .event(event)
            .readOnly(false)
            .build();

        var activity = validateModificationDtoAndSetActivityFields(modificationDto, newActivity);

        activity = activityDao.save(activity);

        return ActivityMapper.toActivityDto(activity);
    }

    @Override
    public ActivityDto updateActivity(long activityId, @NonNull ActivityModificationDto modificationDto) throws NotFoundException {
        // TODO assert admin only

        var activity = activityDao.findById(activityId)
            .orElseThrow(() -> new NotFoundException("Activity not found with id: " + activityId));

        activity = validateModificationDtoAndSetActivityFields(modificationDto, activity);

        activity = activityDao.save(activity);

        return ActivityMapper.toActivityDto(activity);
    }

    Activity validateModificationDtoAndSetActivityFields(ActivityModificationDto modificationDto, Activity activity) throws NotFoundException {
        if (modificationDto.getEndTime().isBefore(modificationDto.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        if (activity.isReadOnly()) {
            throw new BadRequestException("Cannot modify read-only activity");
        }

        activity.setName(modificationDto.getName());
        activity.setDescription(modificationDto.getDescription());
        activity.setStartTime(modificationDto.getStartTime());
        activity.setEndTime(modificationDto.getEndTime());

        var location = locationDao.findById(ConvertUtil.idToLong(modificationDto.getLocationId()))
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + modificationDto.getLocationId()));
        if (location.isReadOnly()) {
            throw new BadRequestException("Cannot assign read-only location to activity");
        }

        activity.setLocation(location);
        return activity;
    }

    @Override
    public void deleteActivity(long activityId) throws NotFoundException {
        // TODO assert admin only

        var activity = activityDao.findById(activityId)
            .orElseThrow(() -> new NotFoundException("Activity not found with id: " + activityId));

        if (activity.isReadOnly()) {
            throw new BadRequestException("Cannot modify read-only activity");
        }

        activityDao.delete(activity);
    }

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

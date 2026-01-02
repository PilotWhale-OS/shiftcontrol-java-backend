package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivityModificationDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivitySuggestionDto;

public interface ActivityService {
    ActivityDto getActivity(long activityId) throws NotFoundException;

    ActivityDto createActivity(long eventId, ActivityModificationDto modificationDto) throws NotFoundException;

    ActivityDto updateActivity(long activityId, ActivityModificationDto modificationDto) throws NotFoundException;

    void deleteActivity(long activityId) throws NotFoundException;

    Collection<ActivityDto> suggestActivitiesForShift(long eventId, ActivitySuggestionDto suggestionDto) throws NotFoundException;
}

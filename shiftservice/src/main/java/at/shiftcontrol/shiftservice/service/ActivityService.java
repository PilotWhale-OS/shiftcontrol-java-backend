package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivityModificationDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivitySuggestionDto;

public interface ActivityService {
    ActivityDto getActivity(long activityId);

    Collection<ActivityDto> getActivitiesForEvent(long eventId);

    ActivityDto createActivity(long eventId, ActivityModificationDto modificationDto);

    ActivityDto updateActivity(long activityId, ActivityModificationDto modificationDto);

    void deleteActivity(long activityId);

    Collection<ActivityDto> suggestActivitiesForShift(long eventId, ActivitySuggestionDto suggestionDto);
}

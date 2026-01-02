package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.dto.ActivitySuggestionDto;

public interface ActivityService {
    
    Collection<ActivityDto> suggestActivitiesForShift(long eventId, ActivitySuggestionDto suggestionDto) throws NotFoundException;
}

package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;

public interface TimeConstraintService {
    Collection<TimeConstraintDto> getTimeConstraints(String userId, long eventId);

    TimeConstraintDto createTimeConstraint(TimeConstraintCreateDto createDto, String userId, long eventId) throws ConflictException;

    void delete(long timeConstraintId) throws NotFoundException;
}

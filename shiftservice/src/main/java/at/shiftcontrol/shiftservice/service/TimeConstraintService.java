package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import lombok.NonNull;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;

public interface TimeConstraintService {
    Collection<TimeConstraintDto> getTimeConstraints(@NonNull String userId, long eventId);

    TimeConstraintDto createTimeConstraint(@NonNull TimeConstraintCreateDto createDto, @NonNull String userId, long eventId) throws ConflictException;

    void delete(long timeConstraintId) throws NotFoundException;
}

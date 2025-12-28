package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import lombok.NonNull;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;

public interface PositionSlotService {
    PositionSlotDto findById(@NonNull Long id) throws NotFoundException;

    AssignmentDto join(@NonNull Long positionSlotId, @NonNull String volunteerId) throws NotFoundException, ConflictException;

    void leave(@NonNull Long positionSlotId, @NonNull Long volunteerId);

    Collection<AssignmentDto> getAssignments(@NonNull Long positionSlotId) throws NotFoundException;

    void setPreference(@NonNull String volunteerId, long positionSlotId, int preference);

    int getPreference(@NonNull String volunteerId, long positionSlotId);

    AssignmentDto createAuction(Long positionSlotId, String currentUserId);

    AssignmentDto claimAuction(Long positionSlotId, String offeringUserId, String currentUserId) throws NotFoundException, ConflictException;

    AssignmentDto cancelAuction(Long positionSlotId, String currentUserId);
}

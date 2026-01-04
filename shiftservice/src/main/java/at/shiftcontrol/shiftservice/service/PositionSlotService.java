package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotModificationDto;
import lombok.NonNull;

public interface PositionSlotService {
    PositionSlotDto findById(@NonNull Long id) throws NotFoundException, ForbiddenException;

    AssignmentDto join(@NonNull Long positionSlotId, @NonNull String currentUserId) throws NotFoundException, ConflictException, ForbiddenException;

    void leave(@NonNull Long positionSlotId, @NonNull String currentUserId) throws NotFoundException, ForbiddenException;

    Collection<AssignmentDto> getAssignments(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException;

    void setPreference(@NonNull String currentUserId, long positionSlotId, int preference) throws NotFoundException, ForbiddenException;

    int getPreference(@NonNull String currentUserId, long positionSlotId) throws ForbiddenException, NotFoundException;

    AssignmentDto createAuction(@NonNull Long positionSlotId, @NonNull String currentUserId);

    AssignmentDto claimAuction(@NonNull Long positionSlotId, @NonNull String offeringUserId, @NonNull String currentUserId)
        throws NotFoundException, ConflictException, ForbiddenException;

    AssignmentDto cancelAuction(@NonNull Long positionSlotId, @NonNull String currentUserId) throws ForbiddenException;

    PositionSlotDto createPositionSlot(@NonNull Long shiftId, @NonNull PositionSlotModificationDto modificationDto) throws NotFoundException,
        ForbiddenException;

    PositionSlotDto updatePositionSlot(@NonNull Long positionSlotId, @NonNull PositionSlotModificationDto modificationDto)
        throws NotFoundException, ForbiddenException;

    void deletePositionSlot(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException;
}

package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.Authorities;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotJoinErrorDto;
import at.shiftcontrol.shiftservice.mapper.AssignmentMapper;
import at.shiftcontrol.shiftservice.mapper.PositionSlotMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.PositionSlotService;

@RequiredArgsConstructor
@Service
public class PositionSlotServiceImpl implements PositionSlotService {
    private final PositionSlotDao positionSlotDao;
    private final VolunteerDao volunteerDao;

    private final ApplicationUserProvider userProvider;
    private final EligibilityService eligibilityService;

    @Override
    //Todo: Calculate SignUpState
    public PositionSlotDto findById(Long id) throws NotFoundException {
        return positionSlotDao.findById(id)
            .map(positionSlot -> PositionSlotMapper.toDto(positionSlot, null))
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
    }

    @Override
    public AssignmentDto join(Long positionSlotId, Long userId) throws NotFoundException, ConflictException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        var volunteer = volunteerDao.findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        var signupState = eligibilityService.getSignupStateForPositionSlot(positionSlot, volunteer);

        switch (signupState) {
            case SIGNED_UP:
                throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
            case FULL:
                // Position is full
                throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
            case NOT_ELIGIBLE:
                if (!userProvider.currentUserHasAuthority(Authorities.CAN_JOIN_UNELIGIBLE_POSITIONS)) {
                    // User is not allowed to join
                    throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
                }
                // All good, proceed with signup
                break;
            case SIGNUP_POSSIBLE:
                // All good, proceed with signup
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + signupState);
        }
        //Todo: Implement actual joining logic


        //Todo: Send Eventbus event
        return null;
    }

    @Override
    public void leave(Long positionSlotId, Long userId) {
        //Todo: Checks are needed if the user can leave
    }

    @Override
    public Collection<AssignmentDto> getAssignments(Long positionSlotId) throws NotFoundException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        return AssignmentMapper.toDto(positionSlot.getAssignments());
    }
}

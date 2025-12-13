package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.mapper.PositionSlotMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import static at.shiftcontrol.shiftservice.type.PositionSignupState.LOCKED;
import static at.shiftcontrol.shiftservice.type.PositionSignupState.NOT_ELIGIBLE;

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
    public AssignmentDto join(Long positionSlotId, Long userId) throws NotFoundException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        var volunteer = volunteerDao.findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        var signupState = eligibilityService.getSignupStateForPositionSlot(positionSlot, volunteer);

        //Todo: Handle bad states properly, if bad case return other DTO with 409 conflict
        switch (signupState) {
            case SIGNED_UP:
                // User is already signed up
                return null;
            case FULL:
                // Position is full
                return null;
            case LOCKED, NOT_ELIGIBLE:
                if (!userProvider.currentUserHasAuthority("shiftcontrol.shiftservice.manage.positions")) {
                    // User is not allowed to join
                    return null;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + signupState);
        }


        if ((signupState == LOCKED || signupState == NOT_ELIGIBLE) && !userProvider.currentUserHasAuthority("shiftcontrol.shiftservice.manage.positions")) {
            // User is not allowed to join
            //Todo: Handle accordingly
            return null;
        }





        return null;
    }

    @Override
    public void leave(Long positionSlotId, Long userId) {
    }

    @Override
    public Collection<AssignmentDto> getAssignments(Long positionSlotId) {
        return List.of();
    }
}

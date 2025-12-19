package at.shiftcontrol.shiftservice.service.impl;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {
    private final VolunteerDao volunteerDao;
    private final PositionSlotDao positionSlotDao;

    @Override
    public PositionSignupState getSignupStateForPositionSlot(Long positionSlotId, Long userId) throws NotFoundException {
        return getSignupStateForPositionSlot(
            positionSlotDao.findById(positionSlotId).orElseThrow(() -> new NotFoundException("PositionSlot with ID %d not found".formatted(positionSlotId))),
            volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer with ID %d not found".formatted(userId))
            ));
    }

    @Override
    public PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, Volunteer volunteer) {
        if (positionSlot.getAssignments().size() >= positionSlot.getDesiredVolunteerCount()) {
            return PositionSignupState.FULL;
        }
        if (isSignedUp(positionSlot, volunteer)) {
            return PositionSignupState.SIGNED_UP;
        }
        if (!volunteer.getRoles().contains(positionSlot.getRole())) {
            return PositionSignupState.NOT_ELIGIBLE;
        }
        return PositionSignupState.SIGNUP_POSSIBLE;
    }

    private boolean isSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        for (var assignment : positionSlot.getAssignments()) {
            if (assignment.getAssignedVolunteer().getId() == volunteer.getId()) {
                return true;
            }
        }
        return false;
    }
}

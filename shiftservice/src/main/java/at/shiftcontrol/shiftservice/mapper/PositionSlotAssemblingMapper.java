package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import at.shiftcontrol.lib.type.AssignmentStatus;

import at.shiftcontrol.shiftservice.dao.AssignmentDao;

import at.shiftcontrol.shiftservice.dao.ShiftDao;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.PositionSignupState;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsDto;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;
import static at.shiftcontrol.shiftservice.mapper.AssignmentAssemblingMapper.ACTIVE_AUCTION_STATES;

@RequiredArgsConstructor
@Service
public class PositionSlotAssemblingMapper {
    private final EligibilityService eligibilityService;
    private final RewardPointsCalculator rewardPointsCalculator;
    private final ApplicationUserProvider applicationUserProvider;
    private final VolunteerDao volunteerDao;
    private final AssignmentDao assignmentDao;
    private final PositionSlotDao positionSlotDao;
    private final AssignmentAssemblingMapper assignmentAssemblingMapper;
    private final TradeMapper tradeMapper;
    private final VolunteerAssemblingMapper volunteerAssemblingMapper;

    public PositionSlotDto assemble(@NonNull PositionSlot positionSlot) {
        var volunteer = volunteerDao.getById(applicationUserProvider.getCurrentUser().getUserId());
        var preferenceValue = positionSlotDao.getPreference(volunteer.getId(), positionSlot.getId());
        var currentRewardPoints = rewardPointsCalculator.calculateForAssignment(positionSlot).rewardPoints();
        var rewardPointsConfigHash = rewardPointsCalculator.calculatePointsConfigHash(positionSlot);

        var rewardPointsDto = RewardPointsDto.builder()
            .currentRewardPoints(currentRewardPoints)
            .overrideRewardPoints(positionSlot.getOverrideRewardPoints())
            .rewardPointsConfigHash(rewardPointsConfigHash)
            .build();

        // calculates SignupState for current user and
        // gets all trade offers for this slot for the current user
        return toDto(positionSlot,
            eligibilityService.getSignupStateForPositionSlot(positionSlot, volunteer),
            filterTradesForUser(positionSlot.getAssignments(), volunteer.getId()),
            preferenceValue,
            rewardPointsDto);
    }

    public Collection<PositionSlotDto> assemble(@NonNull Collection<PositionSlot> positionSlots) {
        return positionSlots.stream().map(this::assemble).toList();
    }

    private Collection<AssignmentSwitchRequest> filterTradesForUser(Collection<Assignment> assignments, String userId) {
        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptyList();
        }
        return assignments.stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId().equals(userId))
            .flatMap(assignment -> assignment.getIncomingSwitchRequests().stream())
            .toList();
    }

    public PositionSlotDto toDto(@NonNull PositionSlot positionSlot, @NonNull PositionSignupState positionSignupState,
                                        Collection<AssignmentSwitchRequest> tradesForUser, int preferenceValue, RewardPointsDto rewardPointsDto) {
        Collection<AssignmentDto> assignmentDtos;
        Collection<AssignmentDto> auctionDtos;

        var activeAssignments = assignmentDao.getActiveAssignmentsOfSlot(positionSlot.getId());
        if (activeAssignments == null) {
            assignmentDtos = null;
            auctionDtos = null;
        } else {
            assignmentDtos = assignmentAssemblingMapper.toDto(activeAssignments);
            auctionDtos = assignmentAssemblingMapper.toDto(activeAssignments.stream()
                .filter(a -> ACTIVE_AUCTION_STATES.contains(a.getStatus()))
                .toList()); // get open auctions for this slot
        }
        return new PositionSlotDto(
            String.valueOf(positionSlot.getId()),
            positionSlot.getName(),
            positionSlot.getDescription(),
            positionSlot.isSkipAutoAssignment(),
            String.valueOf(positionSlot.getShift().getId()),
            positionSlot.getRole() == null ? null : RoleMapper.toRoleDto(positionSlot.getRole()),
            assignmentDtos,
            positionSlot.getDesiredVolunteerCount(),
            positionSignupState,
            tradeMapper.toTradeInfoDto(tradesForUser),
            auctionDtos,
            preferenceValue,
            positionSlot.getShift().getShiftPlan().getLockStatus(),
            rewardPointsDto);
    }

    public TradeCandidatesDto tradeCandidatesDto(@NonNull PositionSlot positionSlot, Collection<Volunteer> volunteers) {
        var shift = positionSlot.getShift();
        return new TradeCandidatesDto(
            assemble(positionSlot),
            shift.getName(),
            shift.getStartTime(),
            volunteerAssemblingMapper.toDto(volunteers)
        );
    }
}

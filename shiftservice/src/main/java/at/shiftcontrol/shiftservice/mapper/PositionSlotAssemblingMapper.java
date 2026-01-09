package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.type.PositionSignupState;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;

@RequiredArgsConstructor
@Service
public class PositionSlotAssemblingMapper {
    private final EligibilityService eligibilityService;
    private final UserProfileService userProfileService;
    private final RewardPointsCalculator rewardPointsCalculator;
    private final ApplicationUserProvider applicationUserProvider;
    private final VolunteerDao volunteerDao;
    private final PositionSlotDao positionSlotDao;

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

    public static PositionSlotDto toDto(@NonNull PositionSlot positionSlot, @NonNull PositionSignupState positionSignupState,
                                        Collection<AssignmentSwitchRequest> tradesForUser, int preferenceValue, RewardPointsDto rewardPointsDto) {
        var assignments = positionSlot.getAssignments();
        Collection<AssignmentDto> assignmentDtos;
        Collection<AssignmentDto> auctionDtos;

        if (assignments == null) {
            assignmentDtos = null;
            auctionDtos = null;
        } else {
            assignmentDtos = AssignmentMapper.toDto(positionSlot.getAssignments());
            auctionDtos = AssignmentMapper.toAuctionDto(assignments); // get open auctions for this slot
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
            TradeMapper.toTradeInfoDto(tradesForUser),
            auctionDtos,
            preferenceValue,
            positionSlot.getShift().getShiftPlan().getLockStatus(),
            rewardPointsDto);
    }

    public TradeCandidatesDto tradeCandidatesDto(@NonNull PositionSlot positionSlot, Collection<Volunteer> volunteers) {
        Collection<AccountInfoDto> accountInfoDtos = volunteers.stream().map(
            v -> {
                try {
                    return userProfileService.getUserProfile(v.getId()).getAccount();
                } catch (NotFoundException | ForbiddenException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        return new TradeCandidatesDto(
            String.valueOf(positionSlot.getId()),
            accountInfoDtos
        );
    }
}

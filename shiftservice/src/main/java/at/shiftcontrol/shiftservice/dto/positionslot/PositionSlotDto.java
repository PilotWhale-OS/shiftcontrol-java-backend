package at.shiftcontrol.shiftservice.dto.positionslot;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.TradeInfoDto;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.type.PositionSignupState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionSlotDto {
    @NotNull
    private String id;

    @NotNull
    private String name;

    private String description;

    @NotNull
    private boolean skipAutoAssignment;

    @NotNull
    private String associatedShiftId;

    @Valid
    private RoleDto role;

    @NotNull
    @Valid
    private Collection<VolunteerDto> assignedVolunteers;

    @NotNull
    private int desiredVolunteerCount;

    @NotNull
    private int rewardPoints;

    /**
     * Specific for the current user's signup state for this position slot.
     */
    @NotNull
    private PositionSignupState positionSignupState;

    /**
     * Specific for the current user's offered trades for this position slot.
     */
    @NotNull
    @Valid
    private Collection<TradeInfoDto> tradeInfoDtos;

    /**
     * Specific for the current user's available auctions of this position slot.
     */
    @NotNull
    @Valid
    private Collection<AssignmentDto> auctions;

    /**
     * Specific for the current user's preference value for this position slot.
     */
    @NotNull
    private int preferenceValue;
}

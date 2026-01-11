package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCandidatesDto {
    @NotNull
    private PositionSlotDto ownPosition;

    @NotNull
    @Valid
    private Collection<VolunteerDto> eligibleTradeRecipients;
}

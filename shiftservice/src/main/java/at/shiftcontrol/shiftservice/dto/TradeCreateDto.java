package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.UserProfile.VolunteerDto;

@Data
@Builder
public class TradeCreateDto {
    @NotNull
    private String offeredPositionSlotId;
    @NotNull
    private String requestedPositionSlotId;
    @NotNull
    private VolunteerDto offeringVolunteer;
}

package at.shiftcontrol.shiftservice.dto.trade;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

@Data
@Builder
public class TradeCreateDto {
    @NotNull
    private String offeredPositionSlotId;

    @NotNull
    private String requestedPositionSlotId;

    @NotNull
    @Valid
    private Collection<VolunteerDto> requestedVolunteers;
}

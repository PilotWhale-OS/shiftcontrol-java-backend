package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

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
    private Collection<VolunteerDto> requestedVolunteers;
}

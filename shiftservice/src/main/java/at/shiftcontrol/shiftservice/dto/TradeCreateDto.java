package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

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

package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeCreateDto {
    @NotNull
    private String offeredShiftId;

    @NotNull
    private String requestedShiftId;

    @NotNull
    private VolunteerDto offeringVolunteer;
}

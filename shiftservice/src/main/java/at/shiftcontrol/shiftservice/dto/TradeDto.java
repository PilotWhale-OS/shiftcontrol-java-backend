package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.TradeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TradeDto {
    @NotNull
    private String id;

    @NotNull
    private ShiftDto offeredShift;

    @NotNull
    private ShiftDto requestedShift;

    @NotNull
    private VolunteerDto offeringVolunteer;

    private VolunteerDto requestedVolunteer;

    @NotNull
    private TradeStatus status;

    @NotNull
    private Instant createdAt;
}

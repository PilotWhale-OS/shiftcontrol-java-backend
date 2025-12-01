package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeRequestDto {
    @NotNull
    private String id;

    @NotNull
    private String offeredShiftId;

    @NotNull
    private String requestedShiftId;

    @NotNull
    private String volunteerId;
}

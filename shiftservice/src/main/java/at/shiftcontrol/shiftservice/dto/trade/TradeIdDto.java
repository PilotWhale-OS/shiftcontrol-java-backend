package at.shiftcontrol.shiftservice.dto.trade;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeIdDto {
    @NotNull
    private String offeredSlotId;

    @NotNull
    private String offeringVolunteerId;

    @NotNull
    private String requestedSlotId;

    @NotNull
    private String requestingVolunteerId;
}

package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.AuctionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuctionDto {
    @NotNull
    private String id;

    @NotNull
    private ShiftDto shift;

    @NotNull
    private String ownerUserId; // TODO or use displayName here?

    @NotNull
    private AuctionStatus status;
}

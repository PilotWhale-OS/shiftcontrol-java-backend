package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.AuctionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuctionDto {
    @NotNull
    private String id;

    @NotNull
    private AssignmentDto offeringAssignment;

    @NotNull
    private AuctionStatus status;

    @NotNull
    private Instant createdAt;
}

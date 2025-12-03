package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.AuctionStatus;

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

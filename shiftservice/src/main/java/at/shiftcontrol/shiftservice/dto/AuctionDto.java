package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.AuctionStatus;

//Todo: What is this? Auction is basically just an Assignment with an auction AssignmentStatus
@Data
@Builder
@Deprecated
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

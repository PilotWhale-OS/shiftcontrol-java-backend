package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.AuctionStatus;

//Todo: What is this? Auction is basically just an Assignment with an auction AssignmentStatus
@Data
@Builder
@Deprecated
public class AuctionInfoDto {
    private boolean isOpen;
    private String auctionId;
    private VolunteerDto ownerVolunteer;
    private Instant createdAt;
    private AuctionStatus status;
}

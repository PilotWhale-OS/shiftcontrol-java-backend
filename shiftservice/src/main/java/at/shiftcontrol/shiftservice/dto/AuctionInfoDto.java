package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.AuctionStatus;

@Data
@Builder
public class AuctionInfoDto {
    private boolean isOpen;
    private String auctionId;
    private VolunteerDto ownerVolunteer;
    private Instant createdAt;
    private AuctionStatus status;
}

package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.AuctionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuctionInfoDto {
    private boolean isOpen;
    private String auctionId;
    private VolunteerDto ownerVolunteer;
    private Instant createdAt;
    private AuctionStatus status;
}

package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.TradeStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeInfoDto {
    private String offeredPositionSlotId;
    private String requestedPositionSlotId;
    private VolunteerDto offeringVolunteer;
    private TradeStatus status;
    private Instant createdAt;
}

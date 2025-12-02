package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.TradeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TradeInfoDto {
    private boolean isOpen;
    private String tradeId;
    private VolunteerDto offeringVolunteer;
    private Instant createdAt;
    private TradeStatus status;
}

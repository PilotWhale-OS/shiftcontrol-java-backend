package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Data
@Builder
public class TradeInfoDto {
    private boolean isOpen;
    private String tradeId;
    private VolunteerDto offeringVolunteer;
    private Instant createdAt;
    private TradeStatus status;
}

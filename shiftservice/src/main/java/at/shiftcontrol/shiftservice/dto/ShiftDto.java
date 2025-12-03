package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.LockStatus;

@Data
@Builder
public class ShiftDto {
    @NotNull
    private String id;
    @NotNull
    private String name;
    private String shortDescription;
    private String longDescription;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
    @NotNull
    private Collection<ActivityDto> relatedActivities;
    @NotNull
    private Collection<PositionSlotDto> positionSlots;
    private LockStatus lockStatus;
    private TradeInfoDto tradeInfo;
    private AuctionInfoDto auctionInfo;
}

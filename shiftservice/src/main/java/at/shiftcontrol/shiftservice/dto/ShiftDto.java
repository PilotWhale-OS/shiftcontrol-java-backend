package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.LockStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Collection;

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
    private Collection<LocationDto> locations;

    @NotNull
    private Collection<ActivityDto> relatedActivities;

    @NotNull
    private Collection<PositionSlotDto> positionSlots;

    private LockStatus lockStatus;

    private TradeInfoDto tradeInfo;

    private AuctionInfoDto auctionInfo;

}

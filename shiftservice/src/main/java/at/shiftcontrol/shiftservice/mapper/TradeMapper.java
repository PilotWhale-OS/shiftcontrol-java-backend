package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NonNull;

import at.shiftcontrol.lib.entity.AssignmentId;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.TradeInfoDto;

public class TradeMapper {
    public static TradeDto toDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeDto(
            AssignmentMapper.toDto(trade.getOfferingAssignment()),
            AssignmentMapper.toDto(trade.getRequestedAssignment()),
            trade.getStatus(),
            trade.getCreatedAt()
        );
    }

    public static Collection<TradeDto> toDto(@NonNull Collection<AssignmentSwitchRequest> trades) {
        return trades.stream().map(TradeMapper::toDto).toList();
    }

    public static AssignmentSwitchRequestId toEntityId(@NonNull TradeDto tradeDto) {
        AssignmentId offering = AssignmentMapper.toEntityId(tradeDto.getOfferingAssignment());
        AssignmentId requested = AssignmentMapper.toEntityId(tradeDto.getRequestedAssignment());
        return new AssignmentSwitchRequestId(offering, requested);
    }

    public static TradeInfoDto toTradeInfoDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeInfoDto(
            String.valueOf(trade.getOfferingAssignment().getPositionSlot()),
            String.valueOf(trade.getRequestedAssignment().getPositionSlot()),
            VolunteerMapper.toDto(trade.getOfferingAssignment().getAssignedVolunteer()),
            trade.getStatus(),
            trade.getCreatedAt()
        );
    }

    public static Collection<TradeInfoDto> toTradeInfoDto(@NonNull Collection<AssignmentSwitchRequest> trades) {
        return trades.stream().map(TradeMapper::toTradeInfoDto).toList();
    }

}

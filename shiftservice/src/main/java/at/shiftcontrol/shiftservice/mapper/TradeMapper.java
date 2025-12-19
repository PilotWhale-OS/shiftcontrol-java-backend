package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NonNull;

import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;

public class TradeMapper {
    public static TradeDto toDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeDto(
            AssignmentMapper.toDto(trade.getOfferingAssignment()),
            AssignmentMapper.toDto(trade.getRequestedAssignment()),
            trade.getStatus(),
            trade.getCreatedAt()
        );
    }

    public static Collection<TradeDto> toDtos(@NonNull Collection<AssignmentSwitchRequest> trades) {
        return trades.stream().map(TradeMapper::toDto).toList();
    }

    public static AssignmentSwitchRequestId toEntityId(@NonNull TradeDto tradeDto) {
        AssignmentId offering = AssignmentMapper.toEntityId(tradeDto.getOfferingAssignment());
        AssignmentId requested = AssignmentMapper.toEntityId(tradeDto.getRequestedAssignment());
        return new AssignmentSwitchRequestId(offering, requested);
    }
}

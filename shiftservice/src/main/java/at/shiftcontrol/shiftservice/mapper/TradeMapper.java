package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.TradeInfoDto;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;

@RequiredArgsConstructor
@Service
public class TradeMapper {
    private final AssignmentAssemblingMapper assignmentAssemblingMapper;
    private final VolunteerAssemblingMapper volunteerAssemblingMapper;

    public TradeDto toDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeDto(
            assignmentAssemblingMapper.toDto(trade.getOfferingAssignment()),
            assignmentAssemblingMapper.toDto(trade.getRequestedAssignment()),
            trade.getStatus(),
            trade.getCreatedAt()
        );
    }

    public Collection<TradeDto> toDto(@NonNull Collection<AssignmentSwitchRequest> trades) {
        return trades.stream().map(this::toDto).toList();
    }

    public static AssignmentSwitchRequestId toEntityId(@NonNull TradeDto tradeDto) {
        AssignmentId offering = AssignmentAssemblingMapper.toEntityId(tradeDto.getOfferingAssignment());
        AssignmentId requested = AssignmentAssemblingMapper.toEntityId(tradeDto.getRequestedAssignment());
        return new AssignmentSwitchRequestId(offering, requested);
    }

    public TradeInfoDto toTradeInfoDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeInfoDto(
            String.valueOf(trade.getOfferingAssignment().getPositionSlot()),
            String.valueOf(trade.getRequestedAssignment().getPositionSlot()),
            volunteerAssemblingMapper.toDto(trade.getOfferingAssignment().getAssignedVolunteer()),
            trade.getStatus(),
            trade.getCreatedAt()
        );
    }

    public Collection<TradeInfoDto> toTradeInfoDto(@NonNull Collection<AssignmentSwitchRequest> trades) {
        return trades.stream().map(this::toTradeInfoDto).toList();
    }

}

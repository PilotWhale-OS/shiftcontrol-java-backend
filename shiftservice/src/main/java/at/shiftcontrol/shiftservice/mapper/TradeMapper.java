package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.AssignmentId;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.trade.TradeDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeIdDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeInfoDto;

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

    public static AssignmentSwitchRequestId toEntityId(@NonNull TradeIdDto tradeDto) {
        AssignmentId offering = AssignmentId.of(ConvertUtil.idToLong(tradeDto.getOfferedSlotId()), tradeDto.getOfferingVolunteerId());
        AssignmentId requested = AssignmentId.of(ConvertUtil.idToLong(tradeDto.getRequestedSlotId()), tradeDto.getRequestingVolunteerId());
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

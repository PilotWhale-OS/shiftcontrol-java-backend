package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.dto.trade.TradeDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeInfoDto;

@RequiredArgsConstructor
@Service
public class TradeMapper {
    private final AssignmentAssemblingMapper assignmentAssemblingMapper;
    private final VolunteerAssemblingMapper volunteerAssemblingMapper;

    public TradeDto toDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeDto(
            String.valueOf(trade.getId()),
            assignmentAssemblingMapper.toDto(trade.getOfferingAssignment()),
            assignmentAssemblingMapper.toDto(trade.getRequestedAssignment()),
            trade.getStatus(),
            trade.getCreatedAt()
        );
    }

    public Collection<TradeDto> toDto(@NonNull Collection<AssignmentSwitchRequest> trades) {
        return trades.stream().map(this::toDto).toList();
    }

    public TradeInfoDto toTradeInfoDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeInfoDto(
            String.valueOf(trade.getId()),
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

    public static AssignmentSwitchRequest shallowCopy(@NonNull AssignmentSwitchRequest trade) {
        return AssignmentSwitchRequest.builder()
            .offeringAssignment(AssignmentAssemblingMapper.shallowCopy(trade.getOfferingAssignment()))
            .requestedAssignment(AssignmentAssemblingMapper.shallowCopy(trade.getRequestedAssignment()))
            .status(trade.getStatus())
            .createdAt(trade.getCreatedAt())
            .build();
    }
}

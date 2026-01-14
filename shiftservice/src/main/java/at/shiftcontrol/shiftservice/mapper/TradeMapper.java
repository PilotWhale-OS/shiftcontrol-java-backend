package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.AssignmentId;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.TradeAcceptDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.TradeInfoDto;

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

    public static AssignmentSwitchRequestId toEntityId(@NonNull TradeAcceptDto acceptDto, String currentUserId) {
        AssignmentId offering = AssignmentId.of(ConvertUtil.idToLong(acceptDto.getOfferedSlot()), acceptDto.getOfferingVolunteer());
        AssignmentId requested = AssignmentId.of(ConvertUtil.idToLong(acceptDto.getRequestedSlot()), currentUserId);
        return new AssignmentSwitchRequestId(offering, requested);
    }

    public TradeInfoDto toTradeInfoDto(@NonNull AssignmentSwitchRequest trade) {
        return new TradeInfoDto(
            String.valueOf(trade.getOfferingAssignment().getPositionSlot().getId()),
            String.valueOf(trade.getRequestedAssignment().getPositionSlot().getId()),
            trade.getOfferingAssignment().getAcceptedRewardPoints(),
            trade.getRequestedAssignment().getAcceptedRewardPoints(),
            volunteerAssemblingMapper.toDto(trade.getOfferingAssignment().getAssignedVolunteer()),
            volunteerAssemblingMapper.toDto(trade.getRequestedAssignment().getAssignedVolunteer()),
            trade.getStatus(),
            trade.getCreatedAt()
        );
    }

    public Collection<TradeInfoDto> toTradeInfoDto(@NonNull Collection<AssignmentSwitchRequest> trades) {
        return trades.stream().map(this::toTradeInfoDto).toList();
    }
}

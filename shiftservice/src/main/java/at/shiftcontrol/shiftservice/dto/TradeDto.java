package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.TradeStatus;

@Data
@Builder
public class TradeDto {
    @NotNull
    private String id;
    @NotNull
    private AssignmentDto offeringAssignment;
    @NotNull
    private AssignmentDto requestedAssignment;
    @NotNull
    private TradeStatus status;
    @NotNull
    private Instant createdAt;
}

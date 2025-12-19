package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.TradeStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDto {
    @NotNull
    private AssignmentDto offeringAssignment;
    @NotNull
    private AssignmentDto requestedAssignment;
    @NotNull
    private TradeStatus status;
    @NotNull
    private Instant createdAt;
}

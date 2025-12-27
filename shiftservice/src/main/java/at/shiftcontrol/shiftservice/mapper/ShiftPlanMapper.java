package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ShiftPlanMapper {
    public static ShiftPlanDto toShiftPlanDto(ShiftPlan shiftPlan) {
        return ShiftPlanDto.builder()
            .id(String.valueOf(shiftPlan.getId()))
            .name(shiftPlan.getName())
            .shortDescription(shiftPlan.getShortDescription())
            .longDescription(shiftPlan.getLongDescription())
            .build();
    }

    public static List<ShiftPlanDto> toShiftPlanDto(Collection<ShiftPlan> shiftPlans) {
        return shiftPlans.stream()
            .map(ShiftPlanMapper::toShiftPlanDto)
            .toList();
    }
}

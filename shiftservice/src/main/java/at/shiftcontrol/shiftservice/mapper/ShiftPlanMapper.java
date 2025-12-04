package at.shiftcontrol.shiftservice.mapper;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ShiftPlanMapper {
    public static ShiftPlanDto shiftPlanToShiftPlanDto(ShiftPlan shiftPlan) {
        return ShiftPlanDto.builder()
                .id(shiftPlan.getId())
                .name(shiftPlan.getName())
                .shortDescription(shiftPlan.getShortDescription())
                .longDescription(shiftPlan.getLongDescription())
                .build();
    }
}

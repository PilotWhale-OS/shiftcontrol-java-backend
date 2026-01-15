package at.shiftcontrol.shiftservice.service.event;

import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleLayoutDto;

public interface EventScheduleService {
    ShiftPlanScheduleLayoutDto getShiftPlanScheduleLayout(long shiftPlanId, ShiftPlanScheduleFilterDto filterDto);

    ShiftPlanScheduleContentDto getShiftPlanScheduleContent(long shiftPlanId, ShiftPlanScheduleDaySearchDto searchDto)
        ;

    ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(long shiftPlanId);
}

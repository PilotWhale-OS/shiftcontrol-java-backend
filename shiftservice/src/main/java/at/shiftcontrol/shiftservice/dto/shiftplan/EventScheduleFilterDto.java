package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.util.Collection;

import at.shiftcontrol.lib.type.ShiftRelevance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class EventScheduleFilterDto {
    private String shiftName;
    private Collection<ShiftRelevance> shiftRelevances;
    private Collection<String> roleIds;
    private Collection<String> locationIds;
}

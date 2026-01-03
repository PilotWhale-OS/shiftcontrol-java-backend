package at.shiftcontrol.shiftservice.dto.activity;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySuggestionDto {
    @Valid
    private ActivityTimeFilterDto timeFilter;

    private String name;
}

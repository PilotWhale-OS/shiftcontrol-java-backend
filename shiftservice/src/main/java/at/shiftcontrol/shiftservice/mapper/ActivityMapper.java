package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.entity.Activity;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ActivityMapper {
    public static ActivityDto toActivityDto(Activity activity) {
        return new ActivityDto(
            String.valueOf(activity.getId()),
            activity.getName(),
            activity.getDescription(),
            activity.getStartTime(),
            activity.getEndTime(),
            LocationMapper.toLocationDto(activity.getLocation())
        );
    }

    public static Collection<ActivityDto> toActivityDto(Collection<Activity> activities) {
        return activities.stream()
            .map(ActivityMapper::toActivityDto)
            .toList();
    }
}

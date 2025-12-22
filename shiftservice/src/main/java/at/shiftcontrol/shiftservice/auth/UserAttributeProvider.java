package at.shiftcontrol.shiftservice.auth;

import java.util.Set;

import lombok.NonNull;

public interface UserAttributeProvider {
    Set<Long> getPlansWhereUserIsVolunteer(@NonNull String userId);

    Set<Long> getPlansWhereUserIsPlanner(@NonNull String userId);
}

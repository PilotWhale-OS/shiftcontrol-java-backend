package at.shiftcontrol.shiftservice.auth;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAttributeProviderImpl implements UserAttributeProvider {
    private LoadingCache<String, UserAttributes> attributesCache;
    private final VolunteerDao volunteerDao;

    @PostConstruct
    public void init() {
        attributesCache = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterAccess(java.time.Duration.ofMinutes(20))
            .build((userId) -> volunteerDao.findByUserId(userId)
                .map(volunteer -> new UserAttributes(
                    volunteer.getVolunteeringPlans().stream().map(ShiftPlan::getId).collect(Collectors.toUnmodifiableSet()),
                    volunteer.getPlanningPlans().stream().map(ShiftPlan::getId).collect(Collectors.toUnmodifiableSet()))).orElse(null));
    }

    public void invalidateUserCache(String userId) {
        attributesCache.invalidate(userId);
    }

    @Override
    public Set<Long> getPlansWhereUserIsVolunteer(@NonNull String userId) {
        return Optional.ofNullable(attributesCache.get(userId)).map(attributes -> attributes.volunteerPlans).orElse(Collections.EMPTY_SET);
    }

    @Override
    public Set<Long> getPlansWhereUserIsPlanner(@NonNull String userId) {
        return Optional.ofNullable(attributesCache.get(userId)).map(attributes -> attributes.plannerPlans).orElse(Collections.EMPTY_SET);
    }

    @AllArgsConstructor
    private static class UserAttributes {
        Set<Long> volunteerPlans;
        Set<Long> plannerPlans;
    }
}

package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;

public class AssignedUser extends ShiftControlUser {
    private final UserAttributeProvider attributeProvider;

    public AssignedUser(Collection<? extends GrantedAuthority> authorities, String username, String userId, UserAttributeProvider attributeProvider) {
        super(authorities, username, userId);
        this.attributeProvider = attributeProvider;
    }

    @Override
    public boolean isVolunteerInPlan(long shiftPlanId) {
        return getRelevantVolunteerPlans().contains(shiftPlanId);
    }

    @Override
    public boolean isPlannerInPlan(long shiftPlanId) {
        return getRelevantPlannerPlans().contains(shiftPlanId);
    }

    @Override
    public boolean isLockedInPlan(long shiftPlanId) {
        return getLockedPlannerPlans().contains(shiftPlanId);
    }

    public Set<Long> getRelevantVolunteerPlans() {
        return attributeProvider.getPlansWhereUserIsVolunteer(getUserId());
    }

    public Set<Long> getRelevantPlannerPlans() {
        return attributeProvider.getPlansWhereUserIsPlanner(getUserId());
    }

    public Set<Long> getLockedPlannerPlans() {
        return attributeProvider.getPlansWhereUserIsLocked(getUserId());
    }
}

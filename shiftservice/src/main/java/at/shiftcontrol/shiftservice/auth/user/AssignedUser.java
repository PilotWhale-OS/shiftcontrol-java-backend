package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;
import java.util.Set;

import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import org.springframework.security.core.GrantedAuthority;

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

    public Set<Long> getRelevantVolunteerPlans() {
        return attributeProvider.getPlansWhereUserIsVolunteer(getUserId());
    }

    public Set<Long> getRelevantPlannerPlans() {
        return attributeProvider.getPlansWhereUserIsPlanner(getUserId());
    }
}

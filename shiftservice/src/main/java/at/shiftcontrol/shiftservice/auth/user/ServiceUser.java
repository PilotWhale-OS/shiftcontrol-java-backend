package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class ServiceUser extends ShiftControlUser {
    public ServiceUser(Collection<? extends GrantedAuthority> authorities, String username, String userId) {
        super(authorities, username, userId);
    }

    @Override
    public boolean isVolunteerInPlan(long shiftPlanId) {
        return false;
    }

    @Override
    public boolean isPlannerInPlan(long shiftPlanId) {
        return false;
    }

    @Override
    public boolean isLockedInPlan(long shiftPlanId) {
        return false;
    }
}

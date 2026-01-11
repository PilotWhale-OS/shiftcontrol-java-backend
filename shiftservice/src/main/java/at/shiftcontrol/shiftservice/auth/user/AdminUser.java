package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class AdminUser extends ShiftControlUser {
    public AdminUser(Collection<? extends GrantedAuthority> authorities, String username, String userId) {
        super(authorities, username, userId);
    }

    public boolean isVolunteerInPlan(long shiftPlanId) {
        return true;
    }

    public boolean isPlannerInPlan(long shiftPlanId) {
        return true;
    }

    @Override
    public boolean isLockedInPlan(long shiftPlanId) {
        return false;
    }
}

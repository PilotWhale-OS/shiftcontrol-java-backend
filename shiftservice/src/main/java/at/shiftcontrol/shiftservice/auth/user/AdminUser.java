package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.core.GrantedAuthority;

public class AdminUser extends ShiftControlUser {

    public AdminUser(Collection<? extends GrantedAuthority> authorities, String username, String userId) {
        super(authorities, username, userId);
    }

    public boolean isVolunteerInPlan(long shiftPlanId) {
        // not needed for admin user
        throw new NotImplementedException("isVolunteerInPlan is not implemented for AdminUser");
    }

    public boolean isPlannerInPlan(long shiftPlanId) {
        throw new NotImplementedException("isPlannerInPlan is not implemented for AdminUser");
    }
}

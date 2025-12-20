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
    public boolean isVolunteerInShift(long shiftId) {
        return getAssignedVolunteerShifts().contains(shiftId);
    }

    @Override
    public boolean isPlannerInShift(long shiftId) {
        return getAssignedPlannerShifts().contains(shiftId);
    }

    public Set<Long> getAssignedVolunteerShifts() {
        return attributeProvider.getPlansWhereUserIsVolunteer(getUserId());
    }

    public Set<Long> getAssignedPlannerShifts() {
        return attributeProvider.getPlansWhereUserIsPlanner(getUserId());
    }
}

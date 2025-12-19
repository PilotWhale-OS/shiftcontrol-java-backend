package at.shiftcontrol.shiftservice.auth.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

public class AssignedUser extends ShiftControlUser {
    public AssignedUser(Collection<? extends GrantedAuthority> authorities, String username, String userId) {
        super(authorities, username, userId);
    }

    @Override
    public boolean isVolunteerInShift(long shiftId) {
        return false;
    }

    @Override
    public boolean isPlannerInShift(long shiftId) {
        return false;
    }

    public Set<Long> getAssignedVolunteerShifts(){
        return null;
    }

    public Set<Long> getAssignedPlannerShifts(){
        return null;
    }
}

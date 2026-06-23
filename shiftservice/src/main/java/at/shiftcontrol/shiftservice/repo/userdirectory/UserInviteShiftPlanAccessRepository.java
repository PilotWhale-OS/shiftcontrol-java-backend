package at.shiftcontrol.shiftservice.repo.userdirectory;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.UserInviteShiftPlanAccess;

public interface UserInviteShiftPlanAccessRepository extends JpaRepository<UserInviteShiftPlanAccess, Long> {
    Collection<UserInviteShiftPlanAccess> findAllByUserInviteId(long userInviteId);
}

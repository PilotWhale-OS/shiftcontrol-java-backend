package at.shiftcontrol.shiftservice.repo.userdirectory;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.type.UserInviteStatus;

public interface UserInviteRepository extends JpaRepository<UserInvite, Long> {
    Optional<UserInvite> findByCode(String code);

    Optional<UserInvite> findFirstByEmailIgnoreCaseAndStatus(String email, UserInviteStatus status);

    Collection<UserInvite> findAllByStatus(UserInviteStatus status);
}

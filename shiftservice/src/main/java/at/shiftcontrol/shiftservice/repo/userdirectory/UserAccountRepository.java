package at.shiftcontrol.shiftservice.repo.userdirectory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findFirstByEmailIgnoreCase(String email);
}

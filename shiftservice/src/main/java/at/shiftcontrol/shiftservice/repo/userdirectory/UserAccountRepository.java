package at.shiftcontrol.shiftservice.repo.userdirectory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import at.shiftcontrol.lib.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findFirstByEmailIgnoreCase(String email);

    @Query("""
        select distinct userAccount
        from UserAccount userAccount
        left join fetch userAccount.externalIdentities externalIdentity
        """)
    List<UserAccount> findAllWithExternalIdentities();

    @Query("""
        select distinct userAccount
        from UserAccount userAccount
        left join fetch userAccount.externalIdentities externalIdentity
        where userAccount.platformAdmin = true
        """)
    List<UserAccount> findAllPlatformAdminsWithExternalIdentities();
}

package at.shiftcontrol.shiftservice.repo.userdirectory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.type.UserInviteStatus;

public interface UserInviteRepository extends JpaRepository<UserInvite, Long> {
    Optional<UserInvite> findByCode(String code);

    Optional<UserInvite> findFirstByEmailIgnoreCaseAndStatus(String email, UserInviteStatus status);

    List<UserInvite> findAllByEmailIgnoreCaseAndStatus(String email, UserInviteStatus status);

    Collection<UserInvite> findAllByStatus(UserInviteStatus status);

    @Query(value = """
        select userInvite.id
        from UserInvite userInvite
        where (:status is null or userInvite.status = :status)
        and (
            :pattern is null
            or lower(coalesce(userInvite.email, '')) like :pattern
            or lower(coalesce(userInvite.preferredUsername, '')) like :pattern
            or lower(coalesce(userInvite.firstName, '')) like :pattern
            or lower(coalesce(userInvite.lastName, '')) like :pattern
            or lower(coalesce(userInvite.displayName, '')) like :pattern
        )
        order by userInvite.createdAt desc, userInvite.id desc
        """, countQuery = """
        select count(userInvite.id)
        from UserInvite userInvite
        where (:status is null or userInvite.status = :status)
        and (
            :pattern is null
            or lower(coalesce(userInvite.email, '')) like :pattern
            or lower(coalesce(userInvite.preferredUsername, '')) like :pattern
            or lower(coalesce(userInvite.firstName, '')) like :pattern
            or lower(coalesce(userInvite.lastName, '')) like :pattern
            or lower(coalesce(userInvite.displayName, '')) like :pattern
        )
        """)
    Page<Long> searchInviteIds(@Param("status") UserInviteStatus status, @Param("pattern") String pattern, Pageable pageable);

    @Query("""
        select userInvite
        from UserInvite userInvite
        where userInvite.id in :inviteIds
        """)
    List<UserInvite> findAllDetailedByIdIn(@Param("inviteIds") Collection<Long> inviteIds);

    boolean existsByCode(String code);
}

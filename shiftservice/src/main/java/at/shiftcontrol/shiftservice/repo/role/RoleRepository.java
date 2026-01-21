package at.shiftcontrol.shiftservice.repo.role;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.lib.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByShiftPlanId(Long shiftPlanId);

    @Query("""
        SELECT DISTINCT r
        FROM Role r
        WHERE r.id in :roleIds
        """)
    Collection<Role> getByIds(Set<Long> roleIds);
}

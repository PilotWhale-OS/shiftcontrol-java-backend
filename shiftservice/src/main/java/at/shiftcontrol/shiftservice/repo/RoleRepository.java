package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.role.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}

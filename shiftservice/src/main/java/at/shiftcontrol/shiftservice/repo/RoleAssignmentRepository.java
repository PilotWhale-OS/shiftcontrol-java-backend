package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.role.RoleAssignment;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {
    List<RoleAssignment> findAllByRole_ShiftPlanIdAndUserId(Long eventId, String userId);
}

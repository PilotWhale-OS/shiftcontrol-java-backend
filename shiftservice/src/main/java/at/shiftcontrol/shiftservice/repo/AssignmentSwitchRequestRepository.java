package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;

@Repository
public interface AssignmentSwitchRequestRepository extends JpaRepository<AssignmentSwitchRequest, AssignmentSwitchRequestId> {
}

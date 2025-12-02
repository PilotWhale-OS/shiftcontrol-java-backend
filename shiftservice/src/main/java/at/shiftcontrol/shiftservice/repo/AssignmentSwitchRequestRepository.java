package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentSwitchRequestRepository extends JpaRepository<AssignmentSwitchRequest, AssignmentSwitchRequestId> {

}
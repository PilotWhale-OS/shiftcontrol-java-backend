package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.PositionConstraint;
import at.shiftcontrol.shiftservice.entity.PositionConstraintId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionConstraintRepository extends JpaRepository<PositionConstraint, PositionConstraintId> {

}
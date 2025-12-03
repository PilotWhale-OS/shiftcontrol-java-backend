package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.PositionConstraint;
import at.shiftcontrol.shiftservice.entity.PositionConstraintId;

@Repository
public interface PositionConstraintRepository extends JpaRepository<PositionConstraint, PositionConstraintId> {
}

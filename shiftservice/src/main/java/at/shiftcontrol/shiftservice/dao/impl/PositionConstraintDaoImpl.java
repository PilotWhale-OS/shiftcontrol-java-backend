package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.PositionConstraint;
import at.shiftcontrol.shiftservice.entity.PositionConstraintId;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.PositionConstraintDao;
import at.shiftcontrol.shiftservice.repo.PositionConstraintRepository;

@RequiredArgsConstructor
@Component
public class PositionConstraintDaoImpl implements PositionConstraintDao {
    private final PositionConstraintRepository positionConstraintRepository;

    @Override
    public Optional<PositionConstraint> findById(PositionConstraintId id) {
        return Optional.empty();
    }

    @Override
    public PositionConstraint save(PositionConstraint entity) {
        return null;
    }

    @Override
    public void delete(PositionConstraint entity) {
    }
}

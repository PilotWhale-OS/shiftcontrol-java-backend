package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;

@RequiredArgsConstructor
@Component
public class AssignmentDaoImpl implements AssignmentDao {
    private final AssignmentRepository assignmentRepository;

    @Override
    public Optional<Assignment> findById(AssignmentId id) {
        return Optional.empty();
    }

    @Override
    public Assignment save(Assignment entity) {
        return null;
    }

    @Override
    public void delete(Assignment entity) {
    }
}

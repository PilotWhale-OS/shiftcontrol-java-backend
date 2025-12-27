package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;

@RequiredArgsConstructor
@Component
public class AssignmentSwitchRequestDaoImpl implements AssignmentSwitchRequestDao {
    private final AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    @Override
    public Optional<AssignmentSwitchRequest> findById(AssignmentSwitchRequestId id) {
        return assignmentSwitchRequestRepository.findById(id);
    }

    @Override
    public AssignmentSwitchRequest save(AssignmentSwitchRequest entity) {
        return assignmentSwitchRequestRepository.save(entity);
    }

    @Override
    public Collection<AssignmentSwitchRequest> saveAll(Collection<AssignmentSwitchRequest> entities) {
        return assignmentSwitchRequestRepository.saveAll(entities);
    }

    @Override
    public void delete(AssignmentSwitchRequest entity) {
        assignmentSwitchRequestRepository.delete(entity);
    }
}

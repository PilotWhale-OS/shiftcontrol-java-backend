package at.shiftcontrol.shiftservice.dao.role.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.role.RoleAssignmentDao;
import at.shiftcontrol.shiftservice.entity.role.RoleAssignment;
import at.shiftcontrol.shiftservice.repo.RoleAssignmentRepository;

@RequiredArgsConstructor
@Component
public class RoleAssignmentDaoImpl implements RoleAssignmentDao {
    private final RoleAssignmentRepository repository;

    @Override
    public List<RoleAssignment> findAllByRole_EventIdAndUserId(Long eventId, String userId) {
        return repository.findAllByRole_EventIdAndUserId(eventId, userId);
    }

    @Override
    public Optional<RoleAssignment> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public RoleAssignment save(RoleAssignment entity) {
        return repository.save(entity);
    }

    @Override
    public Collection<RoleAssignment> saveAll(Collection<RoleAssignment> entities) {
        return repository.saveAll(entities);
    }

    @Override
    public void delete(RoleAssignment entity) {
        repository.delete(entity);
    }
}

package at.shiftcontrol.shiftservice.dao.role.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.repo.RoleRepository;

@RequiredArgsConstructor
@Component
public class RoleDaoImpl implements RoleDao {
    private final RoleRepository roleRepository;

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Collection<Role> findAllById(Collection<Long> roleIds) {
        return roleRepository.findAllById(roleIds);
    }

    @Override
    public Role save(Role entity) {
        return roleRepository.save(entity);
    }

    @Override
    public Collection<Role> saveAll(Collection<Role> entities) {
        return roleRepository.saveAll(entities);
    }

    @Override
    public void delete(Role entity) {
        roleRepository.delete(entity);
    }

    @Override
    public List<Role> findAllByShiftPlanId(Long shiftPlanId) {
        return roleRepository.findAllByShiftPlanId(shiftPlanId);
    }
}

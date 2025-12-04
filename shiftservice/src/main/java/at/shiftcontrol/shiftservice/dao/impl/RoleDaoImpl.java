package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.RoleDao;
import at.shiftcontrol.shiftservice.entity.Role;
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
    public Role save(Role entity) {
        return roleRepository.save(entity);
    }

    @Override
    public void delete(Role entity) {
        roleRepository.delete(entity);
    }
}

package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.Role;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.RoleDao;
import at.shiftcontrol.shiftservice.repo.RoleRepository;

@RequiredArgsConstructor
@Component
public class RoleDaoImpl implements RoleDao {
    private final RoleRepository roleRepository;

    @Override
    public Optional<Role> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Role save(Role entity) {
        return null;
    }

    @Override
    public void delete(Role entity) {
    }
}

package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.RoleDao;
import at.shiftcontrol.shiftservice.repo.RoleRepository;

@RequiredArgsConstructor
@Component
public class RoleDaoImpl implements RoleDao {
    private final RoleRepository roleRepository;
}

package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.RoleDao;
import at.shiftcontrol.shiftservice.repo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RoleDaoImpl implements RoleDao {

    private final RoleRepository roleRepository;

}

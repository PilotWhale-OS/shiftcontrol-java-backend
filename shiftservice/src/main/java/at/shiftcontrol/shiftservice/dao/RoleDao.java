package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.Role;

public interface RoleDao extends BasicDao<Role, Long> {
    Collection<Role> findAllById(Collection<Long> roleIds);
}

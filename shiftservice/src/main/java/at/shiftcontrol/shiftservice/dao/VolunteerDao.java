package at.shiftcontrol.shiftservice.dao;

import java.util.Optional;

import at.shiftcontrol.shiftservice.entity.Volunteer;

public interface VolunteerDao extends BasicDao<Volunteer, Long> {
    Optional<Volunteer> findByUserId(Long userId);
}

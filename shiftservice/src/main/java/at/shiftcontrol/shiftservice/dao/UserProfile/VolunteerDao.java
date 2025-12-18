package at.shiftcontrol.shiftservice.dao.UserProfile;

import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.BasicDao;
import at.shiftcontrol.shiftservice.entity.Volunteer;

public interface VolunteerDao extends BasicDao<Volunteer, Long> {
    Optional<Volunteer> findByUserId(Long userId);
}

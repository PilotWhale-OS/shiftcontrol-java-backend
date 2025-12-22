package at.shiftcontrol.shiftservice.dao.userprofile;

import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.BasicDao;
import at.shiftcontrol.shiftservice.entity.Volunteer;

public interface VolunteerDao extends BasicDao<Volunteer, String> {
    Optional<Volunteer> findByUserId(String userId);
}

package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

}
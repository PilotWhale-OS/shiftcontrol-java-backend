package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}

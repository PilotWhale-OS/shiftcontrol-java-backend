package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.shiftservice.dto.location.LocationSearchDto;

public interface LocationDao extends BasicDao<Location, Long> {
    Collection<Location> findAllByEventId(Long eventId);

    List<Location> search(LocationSearchDto searchDto);
}

package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.location.LocationSearchDto;

import at.shiftcontrol.lib.entity.Location;

public interface LocationDao extends BasicDao<Location, Long> {
    Collection<Location> findAllByEventId(Long eventId);

    Collection<Location> search(LocationSearchDto searchDto);
}

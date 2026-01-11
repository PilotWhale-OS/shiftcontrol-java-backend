package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;

public interface EventDao extends BasicDao<Event, Long> {
    List<Event> search(EventSearchDto searchDto);

    Collection<Event> findAll();
}

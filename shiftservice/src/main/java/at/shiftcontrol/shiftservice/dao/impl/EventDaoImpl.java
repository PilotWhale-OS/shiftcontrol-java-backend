package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.repo.EventRepository;

@RequiredArgsConstructor
@Component
public class EventDaoImpl implements EventDao {
    private final EventRepository eventRepository;
}

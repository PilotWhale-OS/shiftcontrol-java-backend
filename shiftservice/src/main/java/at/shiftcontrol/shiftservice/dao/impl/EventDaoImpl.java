package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.Event;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.repo.EventRepository;

@RequiredArgsConstructor
@Component
public class EventDaoImpl implements EventDao {
    private final EventRepository eventRepository;

    @Override
    public Optional<Event> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Event save(Event entity) {
        return null;
    }

    @Override
    public void delete(Event entity) {
    }
}

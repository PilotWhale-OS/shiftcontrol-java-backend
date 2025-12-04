package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.repo.EventRepository;

@RequiredArgsConstructor
@Component
public class EventDaoImpl implements EventDao {
    private final EventRepository eventRepository;

    @Override
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public Event save(Event entity) {
        return eventRepository.save(entity);
    }

    @Override
    public void delete(Event entity) {
        eventRepository.delete(entity);
    }
}

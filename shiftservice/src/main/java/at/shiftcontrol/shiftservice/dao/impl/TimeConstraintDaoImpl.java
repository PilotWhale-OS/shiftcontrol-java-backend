package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import at.shiftcontrol.shiftservice.dao.TimeConstraintDao;
import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.repo.TimeConstraintRepository;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;

@RequiredArgsConstructor
@Component
public class TimeConstraintDaoImpl implements TimeConstraintDao {
    private final TimeConstraintRepository timeConstraintRepository;

    @Override
    public @NonNull String getName() {
        return "TimeConstraint";
    }

    @Override
    public @NonNull Optional<TimeConstraint> findById(Long id) {
        return timeConstraintRepository.findById(id);
    }

    @Override
    public TimeConstraint save(TimeConstraint entity) {
        return timeConstraintRepository.save(entity);
    }

    @Override
    public Collection<TimeConstraint> saveAll(Collection<TimeConstraint> entities) {
        return timeConstraintRepository.saveAll(entities);
    }

    @Override
    public void delete(TimeConstraint entity) {
        timeConstraintRepository.delete(entity);
    }

    @Override
    public Collection<TimeConstraint> searchByVolunteerAndEvent(String volunteerId, long eventId) {
        return timeConstraintRepository.searchByVolunteerAndEvent(volunteerId, eventId);
    }

    @Override
    public Collection<TimeConstraint> searchByVolunteerAndEventAndType(String volunteerId, long eventId, TimeConstraintType type) {
        return timeConstraintRepository.searchByVolunteerAndEventAndType(volunteerId, eventId, type);
    }
}

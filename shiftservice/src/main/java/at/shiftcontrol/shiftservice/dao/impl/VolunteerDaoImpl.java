package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;

@RequiredArgsConstructor
@Component
public class VolunteerDaoImpl implements VolunteerDao {
    private final VolunteerRepository volunteerRepository;

    @Override
    public Optional<Volunteer> findById(Long id) {
        return volunteerRepository.findById(id);
    }

    @Override
    public Volunteer save(Volunteer entity) {
        return volunteerRepository.save(entity);
    }

    @Override
    public Collection<Volunteer> saveAll(Collection<Volunteer> entities) {
        return volunteerRepository.saveAll(entities);
    }

    @Override
    public void delete(Volunteer entity) {
        volunteerRepository.delete(entity);
    }

    @Override
    public Optional<Volunteer> findByUserId(Long userId) {
        return findById(userId); //Todo: Check if this is correct
    }
}

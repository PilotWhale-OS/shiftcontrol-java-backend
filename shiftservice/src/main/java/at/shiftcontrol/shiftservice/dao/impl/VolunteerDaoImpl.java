package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.Volunteer;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;

@RequiredArgsConstructor
@Component
public class VolunteerDaoImpl implements VolunteerDao {
    private final VolunteerRepository volunteerRepository;

    @Override
    public Optional<Volunteer> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Volunteer save(Volunteer entity) {
        return null;
    }

    @Override
    public void delete(Volunteer entity) {
    }
}

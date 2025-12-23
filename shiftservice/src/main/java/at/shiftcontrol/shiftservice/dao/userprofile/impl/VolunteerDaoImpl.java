package at.shiftcontrol.shiftservice.dao.userprofile.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VolunteerDaoImpl implements VolunteerDao {
    private final VolunteerRepository volunteerRepository;

    @Override
    public Optional<Volunteer> findById(String id) {
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
    public Optional<Volunteer> findByUserId(String userId) {
        return volunteerRepository.findById(userId);
    }
}

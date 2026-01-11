package at.shiftcontrol.shiftservice.dao.userprofile.impl;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VolunteerDaoImpl implements VolunteerDao {
    private final VolunteerRepository volunteerRepository;

    @Override
    public String getName() {
        return "Volunteer";
    }

    @Override
    public Optional<Volunteer> findById(String id) {
        return volunteerRepository.findById(id);
    }

    @Override
    public Collection<Volunteer> findAllByShiftPlan(long id) {
        return volunteerRepository.findAllByShiftPlan(id);
    }

    @Override
    public Collection<Volunteer> findAllPlannersByShiftPlan(long id) {
        return volunteerRepository.findAllPlannersByShiftPlan(id);
    }

    @Override
    public Collection<Volunteer> findAllByEvent(long eventId) {
        return volunteerRepository.findAllByEvent(eventId);
    }

    @Override
    public Collection<Volunteer> findAllPlannersByEvent(long eventId) {
        return volunteerRepository.findAllPlannersByEvent(eventId);
    }

    @Override
    public Collection<Volunteer> findAllByVolunteerIds(Collection<String> volunteerIds) {
        return volunteerRepository.findAllByVolunteerIds(volunteerIds);
    }

    @Override
    public Collection<Volunteer> findAllByPlannerIds(Collection<String> plannerIds) {
        return volunteerRepository.findAllByPlannerIds(plannerIds);
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
    public Collection<Volunteer> findAllByShiftPlanAndVolunteerIds(long shiftPlanId, Collection<String> volunteerIds) {
        return volunteerRepository.findAllByShiftPlanAndVolunteerIds(shiftPlanId, volunteerIds);
    }

    @Override
    public Collection<Volunteer> findAllByEventAndVolunteerIds(long eventId, Collection<String> volunteerIds) {
        return volunteerRepository.findAllByEventAndVolunteerIds(eventId, volunteerIds);
    }

    @Override
    public Collection<Volunteer> findAllByShiftPlanAndPlannerIds(long shiftPlanId, Collection<String> plannerIds) {
        return volunteerRepository.findAllByShiftPlanAndPlannerIds(shiftPlanId, plannerIds);
    }

    @Override
    public Collection<Volunteer> findAllByEventAndPlannerIds(long eventId, Collection<String> plannerIds) {
        return volunteerRepository.findAllByEventAndPlannerIds(eventId, plannerIds);
    }

    @Override
    public Collection<Volunteer> findAll() {
        return volunteerRepository.findAll();
    }
}

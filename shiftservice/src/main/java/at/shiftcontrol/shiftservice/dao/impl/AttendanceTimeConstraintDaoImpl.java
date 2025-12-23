package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AttendanceTimeConstraintDao;
import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;
import at.shiftcontrol.shiftservice.repo.AttendanceTimeConstraintRepository;

@RequiredArgsConstructor
@Component
public class AttendanceTimeConstraintDaoImpl implements AttendanceTimeConstraintDao {
    private final AttendanceTimeConstraintRepository attendanceTimeConstraintRepository;

    @Override
    public Optional<AttendanceTimeConstraint> findById(Long id) {
        return attendanceTimeConstraintRepository.findById(id);
    }

    @Override
    public AttendanceTimeConstraint save(AttendanceTimeConstraint entity) {
        return attendanceTimeConstraintRepository.save(entity);
    }

    @Override
    public Collection<AttendanceTimeConstraint> saveAll(Collection<AttendanceTimeConstraint> entities) {
        return attendanceTimeConstraintRepository.saveAll(entities);
    }

    @Override
    public void delete(AttendanceTimeConstraint entity) {
        attendanceTimeConstraintRepository.delete(entity);
    }
}

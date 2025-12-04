package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AttendanceTimeConstraintDao;
import at.shiftcontrol.shiftservice.repo.AttendanceTimeConstraintRepository;

@RequiredArgsConstructor
@Component
public class AttendanceTimeConstraintDaoImpl implements AttendanceTimeConstraintDao {
    private final AttendanceTimeConstraintRepository attendanceTimeConstraintRepository;

    @Override
    public Optional<AttendanceTimeConstraint> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public AttendanceTimeConstraint save(AttendanceTimeConstraint entity) {
        return null;
    }

    @Override
    public void delete(AttendanceTimeConstraint entity) {
    }
}

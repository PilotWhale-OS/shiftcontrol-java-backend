package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.Attendance;
import at.shiftcontrol.shiftservice.entity.AttendanceId;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AttendanceDao;
import at.shiftcontrol.shiftservice.repo.AttendanceRepository;

@RequiredArgsConstructor
@Component
public class AttendanceDaoImpl implements AttendanceDao {
    private final AttendanceRepository attendanceRepository;

    @Override
    public Optional<Attendance> findById(AttendanceId id) {
        return Optional.empty();
    }

    @Override
    public Attendance save(Attendance entity) {
        return null;
    }

    @Override
    public void delete(Attendance entity) {
    }
}

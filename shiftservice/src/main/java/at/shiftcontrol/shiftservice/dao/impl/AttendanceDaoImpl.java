package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AttendanceDao;
import at.shiftcontrol.shiftservice.entity.Attendance;
import at.shiftcontrol.shiftservice.entity.AttendanceId;
import at.shiftcontrol.shiftservice.repo.AttendanceRepository;

@RequiredArgsConstructor
@Component
public class AttendanceDaoImpl implements AttendanceDao {
    private final AttendanceRepository attendanceRepository;

    @Override
    public Optional<Attendance> findById(AttendanceId id) {
        return attendanceRepository.findById(id);
    }

    @Override
    public Attendance save(Attendance entity) {
        return attendanceRepository.save(entity);
    }

    @Override
    public Collection<Attendance> saveAll(Collection<Attendance> entities) {
        return attendanceRepository.saveAll(entities);
    }

    @Override
    public void delete(Attendance entity) {
        attendanceRepository.delete(entity);
    }
}

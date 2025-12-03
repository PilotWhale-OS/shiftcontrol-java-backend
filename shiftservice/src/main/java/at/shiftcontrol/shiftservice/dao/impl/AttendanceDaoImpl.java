package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AttendanceDao;
import at.shiftcontrol.shiftservice.repo.AttendanceRepository;

@RequiredArgsConstructor
@Component
public class AttendanceDaoImpl implements AttendanceDao {
    private final AttendanceRepository attendanceRepository;
}

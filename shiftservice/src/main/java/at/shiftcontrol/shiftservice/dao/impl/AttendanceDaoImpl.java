package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.AttendanceDao;
import at.shiftcontrol.shiftservice.repo.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AttendanceDaoImpl implements AttendanceDao {

    private final AttendanceRepository attendanceRepository;

}

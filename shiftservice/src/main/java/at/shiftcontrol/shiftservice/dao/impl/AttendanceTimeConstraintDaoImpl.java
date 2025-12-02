package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.AttendanceTimeConstraintDao;
import at.shiftcontrol.shiftservice.repo.AttendanceTimeConstraintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AttendanceTimeConstraintDaoImpl implements AttendanceTimeConstraintDao {

    private final AttendanceTimeConstraintRepository attendanceTimeConstraintRepository;

}

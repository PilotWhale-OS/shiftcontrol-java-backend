package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AttendanceTimeConstraintDao;
import at.shiftcontrol.shiftservice.repo.AttendanceTimeConstraintRepository;

@RequiredArgsConstructor
@Component
public class AttendanceTimeConstraintDaoImpl implements AttendanceTimeConstraintDao {
    private final AttendanceTimeConstraintRepository attendanceTimeConstraintRepository;
}

package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;

@RequiredArgsConstructor
@Component
public class AssignmentSwitchRequestDaoImpl implements AssignmentSwitchRequestDao {
    private final AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;
}

package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;

@RequiredArgsConstructor
@Component
public class ShiftDaoImpl implements ShiftDao {
    private final ShiftRepository shiftRepository;
}

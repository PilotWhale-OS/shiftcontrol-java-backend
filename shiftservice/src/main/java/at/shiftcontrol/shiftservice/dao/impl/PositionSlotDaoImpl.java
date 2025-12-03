package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;

@RequiredArgsConstructor
@Component
public class PositionSlotDaoImpl implements PositionSlotDao {
    private final PositionSlotRepository positionSlotRepository;
}

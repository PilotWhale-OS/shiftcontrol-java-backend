package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PositionSlotDaoImpl implements PositionSlotDao {

    private final PositionSlotRepository positionSlotRepository;

}

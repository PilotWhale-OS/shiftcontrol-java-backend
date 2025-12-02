package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.PositionConstraintDao;
import at.shiftcontrol.shiftservice.repo.PositionConstraintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PositionConstraintDaoImpl implements PositionConstraintDao {

    private final PositionConstraintRepository positionConstraintRepository;

}

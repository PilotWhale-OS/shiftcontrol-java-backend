package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShiftPlanDaoImpl implements ShiftPlanDao {

    private final ShiftPlanRepository shiftPlanRepository;

}

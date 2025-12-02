package at.shiftcontrol.shiftservice.dao.impl;

import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShiftDaoImpl implements ShiftDao {

    private final ShiftRepository shiftRepository;

}

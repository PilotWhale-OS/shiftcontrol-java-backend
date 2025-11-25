package at.shiftcontrol.shiftsystem.service.impl;

import at.shiftcontrol.shiftsystem.dao.ShiftDao;
import at.shiftcontrol.shiftsystem.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftDao shiftDao;
}

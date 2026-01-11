package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.shiftservice.dao.TrustAlertDao;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;
import at.shiftcontrol.shiftservice.mapper.TrustAlertAssemblingMapper;
import at.shiftcontrol.shiftservice.service.TrustAlertService;

public class TrustAlertServiceImpl implements TrustAlertService {
    TrustAlertDao trustAlertDao;
    TrustAlertAssemblingMapper trustAlertAssemblingMapper;

    @Override
    public Collection<TrustAlertDisplayDto> getAllPaginated(long page, long size) {
        return trustAlertAssemblingMapper.toDto(trustAlertDao.getAllPaginated(page, size));
    }

    @Override
    public TrustAlertDisplayDto save(TrustAlert alert) {
        return trustAlertAssemblingMapper.toDto(trustAlertDao.save(alert));
    }
}

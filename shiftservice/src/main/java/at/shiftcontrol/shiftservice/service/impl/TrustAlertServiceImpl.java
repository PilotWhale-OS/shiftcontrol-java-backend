package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.shiftservice.dao.TrustAlertDao;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;
import at.shiftcontrol.shiftservice.mapper.TrustAlertAssemblingMapper;
import at.shiftcontrol.shiftservice.service.TrustAlertService;

@Service
@RequiredArgsConstructor
public class TrustAlertServiceImpl implements TrustAlertService {
    private final TrustAlertDao trustAlertDao;
    private final TrustAlertAssemblingMapper trustAlertAssemblingMapper;

    @Override
    public Collection<TrustAlertDisplayDto> getAllPaginated(long page, long size) {
        return trustAlertAssemblingMapper.toDto(trustAlertDao.getAllPaginated(page, size));
    }

    @Override
    public TrustAlertDisplayDto save(TrustAlertDto alert) {
        // TODO publish event
        TrustAlert entity = trustAlertAssemblingMapper.toEntity(alert);
        return trustAlertAssemblingMapper.toDto(trustAlertDao.save(entity));
    }
}

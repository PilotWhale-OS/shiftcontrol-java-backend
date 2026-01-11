package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.TrustAlertEvent;
import at.shiftcontrol.shiftservice.dao.TrustAlertDao;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;
import at.shiftcontrol.shiftservice.mapper.TrustAlertAssemblingMapper;
import at.shiftcontrol.shiftservice.service.TrustAlertService;

@Service
@RequiredArgsConstructor
public class TrustAlertServiceImpl implements TrustAlertService {
    private final TrustAlertDao trustAlertDao;
    private final TrustAlertAssemblingMapper trustAlertAssemblingMapper;
    private final ApplicationEventPublisher publisher;

    @Override
    public Collection<TrustAlertDisplayDto> getAllPaginated(long page, long size) {
        return trustAlertAssemblingMapper.toDto(trustAlertDao.getAllPaginated(page, size));
    }

    @Override
    public TrustAlertDisplayDto save(TrustAlertDto alert) {
        // TODO add entities in notification service
        TrustAlert entity = trustAlertAssemblingMapper.toEntity(alert);
        entity = trustAlertDao.save(entity);

        publisher.publishEvent(TrustAlertEvent.of(
            RoutingKeys.format(RoutingKeys.TRUST_ALERT_RECEIVED,
                Map.of("alertId", String.valueOf(entity.getId()))
            ), entity
        ));

        return trustAlertAssemblingMapper.toDto(entity);
    }
}

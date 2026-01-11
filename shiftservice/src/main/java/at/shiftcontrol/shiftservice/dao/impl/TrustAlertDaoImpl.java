package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.shiftservice.dao.TrustAlertDao;
import at.shiftcontrol.shiftservice.repo.TrustAlertRepository;

@RequiredArgsConstructor
@Component
public class TrustAlertDaoImpl implements TrustAlertDao {

    TrustAlertRepository repository;

    @Override
    public Collection<TrustAlert> findAllByEventId(long eventId) {
        return repository.findAllByEventId(eventId);
    }
}

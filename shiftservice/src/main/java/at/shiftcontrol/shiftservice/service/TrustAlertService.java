package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;

public interface TrustAlertService {
    Collection<TrustAlertDisplayDto> getAllPaginated(long  page, long size);

    TrustAlertDisplayDto save(TrustAlert alert);
}

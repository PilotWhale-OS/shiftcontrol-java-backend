package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;

public interface TrustAlertService {
    Collection<TrustAlertDisplayDto> getAllPaginated(long  page, long size);

    TrustAlertDisplayDto save(TrustAlertDto alert);
}

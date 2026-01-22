package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;

public interface TrustAlertService {
    PaginationDto<TrustAlertDisplayDto> getAllPaginated(int  page, int size);

    TrustAlertDisplayDto save(TrustAlertDto alert);

    void delete(long id);
}

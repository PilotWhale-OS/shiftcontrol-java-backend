package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;

public class TrustAlertAssemblingMapper {

    public TrustAlertDisplayDto toDto(TrustAlert alert) {
        return TrustAlertDisplayDto.builder()
            .id(String.valueOf(alert.getId()))
            // TODO map volunteer when user assembling mapper is available
            .alertType(alert.getAlertType())
            .createdAt(alert.getCreatedAt())
            .build();
    }

    public Collection<TrustAlertDisplayDto> toDto(Collection<TrustAlert> alerts) {
        if (alerts == null) {
            return List.of();
        }
        return alerts.stream().map(this::toDto).toList();
    }
}

package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.dto.TrustAlertDto;
import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.TrustAlertDisplayDto;

@Component
@RequiredArgsConstructor
public class TrustAlertAssemblingMapper {
    private final VolunteerDao volunteerDao;

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

    public TrustAlert toEntity(TrustAlertDto alert) {
        return TrustAlert.builder()
            .volunteer(volunteerDao.getById(alert.getUserId()))
            .alertType(alert.getAlertType())
            .createdAt(alert.getCreatedAt())
            .build();
    }
}

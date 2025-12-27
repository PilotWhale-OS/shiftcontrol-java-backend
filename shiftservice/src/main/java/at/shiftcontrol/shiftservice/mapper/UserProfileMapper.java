package at.shiftcontrol.shiftservice.mapper;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;

@RequiredArgsConstructor
@Service
public class UserProfileMapper {
    public static AccountInfoDto toAccountInfoDto(UserRepresentation user) {
        var userTypeAttr = user.firstAttribute("userType");
        var userType = userTypeAttr == null ? UserType.ASSIGNED : UserType.valueOf(userTypeAttr);
        return AccountInfoDto.builder()
            .userType(userType)
            .id(user.getId())
            .fistName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }
}

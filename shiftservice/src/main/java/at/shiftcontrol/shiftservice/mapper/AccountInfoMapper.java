package at.shiftcontrol.shiftservice.mapper;

import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AccountInfoMapper {
    public static AccountInfoDto toDto(UserRepresentation user) {
        var userTypeAttr = user.firstAttribute("userType");
        var userType = userTypeAttr == null ? UserType.ASSIGNED : UserType.valueOf(userTypeAttr);
        return AccountInfoDto.builder()
            .volunteer(VolunteerAssemblingMapper.toDtoFromUser(user))
            .username(user.getUsername())
            .email(user.getEmail())
            .userType(userType)
            .build();
    }
}

package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;

import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AccountInfoMapper {

    public static AccountInfoDto toDto(UserRepresentation user){
        if(user == null){
            return null;
        }
        AccountInfoDto dto = AccountInfoDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fistName(user.getFirstName())
            .lastName(user.getLastName())
            .userType(UserType.ASSIGNED) // TODO: determine user type properly? not really relevant here. could also use a slim DTO
            .build();

        return dto;
    }
}

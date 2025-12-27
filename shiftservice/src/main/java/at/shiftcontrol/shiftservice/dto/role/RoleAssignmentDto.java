package at.shiftcontrol.shiftservice.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleAssignmentDto {
    Long id;
    Long roleId;
    String userId;
}

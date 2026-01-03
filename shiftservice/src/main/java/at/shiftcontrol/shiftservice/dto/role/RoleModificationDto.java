package at.shiftcontrol.shiftservice.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleModificationDto {
    @NotNull
    private String name;

    private String description;

    @NotNull
    private boolean selfAssignable;
}

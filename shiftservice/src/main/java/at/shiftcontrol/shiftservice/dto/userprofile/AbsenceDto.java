package at.shiftcontrol.shiftservice.dto.userprofile;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceDto {
    @NotNull
    Long userId;
    @NotNull
    LocalDate from;
    @NotNull
    LocalDate to;
}

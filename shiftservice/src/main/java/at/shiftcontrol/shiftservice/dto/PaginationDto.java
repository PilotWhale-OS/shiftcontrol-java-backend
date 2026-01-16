package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class PaginationDto<T> {
    @NotNull
    private long pages;

    @NotNull
    private long page;

    @NotNull
    private long total;

    @NotNull
    @Valid
    private Collection<T> items;
}

package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.PaginationDto;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PaginationMapper {
    public static <T> PaginationDto<T> toPaginationDto(long pageSize, long page, long total, Collection<T> items) {
        long pages = pageSize == 0
            ? 0
            : (total + pageSize - 1) / pageSize;
        return PaginationDto.<T>builder()
            .pages(pages)
            .page(page)
            .total(total)
            .items(items)
            .build();
    }
}

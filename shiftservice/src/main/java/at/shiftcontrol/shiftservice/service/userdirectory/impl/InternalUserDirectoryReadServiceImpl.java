package at.shiftcontrol.shiftservice.service.userdirectory.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.user.ContactInfoDto;
import at.shiftcontrol.shiftservice.dto.user.UserSearchDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.mapper.AccountInfoMapper;
import at.shiftcontrol.shiftservice.mapper.PaginationMapper;
import at.shiftcontrol.shiftservice.mapper.UserAssemblingMapper;
import at.shiftcontrol.shiftservice.service.userdirectory.InternalUserDirectoryReadService;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@Service
@RequiredArgsConstructor
public class InternalUserDirectoryReadServiceImpl implements InternalUserDirectoryReadService {
    private final UserDirectoryService userDirectoryService;

    @Override
    public @NonNull AccountInfoDto getUser(@NonNull String userId) {
        return AccountInfoMapper.toDto(userDirectoryService.getUserById(userId));
    }

    @Override
    public @NonNull Collection<AccountInfoDto> getUsers(@NonNull Collection<String> userIds) {
        return userDirectoryService.getUserByIds(userIds).stream()
            .map(AccountInfoMapper::toDto)
            .toList();
    }

    @Override
    public @NonNull Collection<ContactInfoDto> getContacts(@NonNull Collection<String> userIds) {
        return userDirectoryService.getUserByIds(userIds).stream()
            .map(UserAssemblingMapper::toContactInfoDto)
            .toList();
    }

    @Override
    public @NonNull PaginationDto<AccountInfoDto> searchUsers(int page, int size, @NonNull UserSearchDto searchDto) {
        List<DirectoryUser> filteredUsers = filterUsers(searchDto, userDirectoryService.getAllUsers()).stream()
            .sorted(Comparator
                .comparing((DirectoryUser user) -> safeLower(user.lastName()))
                .thenComparing(user -> safeLower(user.firstName()))
                .thenComparing(user -> safeLower(user.username())))
            .toList();

        int fromIndex = page * size;
        if (fromIndex >= filteredUsers.size()) {
            return PaginationMapper.toPaginationDto(size, page, filteredUsers.size(), List.of());
        }

        int toIndex = Math.min(fromIndex + size, filteredUsers.size());
        return PaginationMapper.toPaginationDto(
            size,
            page,
            filteredUsers.size(),
            filteredUsers.subList(fromIndex, toIndex).stream().map(AccountInfoMapper::toDto).toList()
        );
    }

    private static List<DirectoryUser> filterUsers(UserSearchDto searchDto, List<DirectoryUser> users) {
        if (searchDto.getName() == null || searchDto.getName().isEmpty()) {
            return users;
        }
        String nameLower = searchDto.getName().toLowerCase().trim();
        return users.stream()
            .filter(user ->
                safeLower(user.username()).contains(nameLower)
                    || safeLower(user.firstName()).contains(nameLower)
                    || safeLower(user.lastName()).contains(nameLower)
            )
            .toList();
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}

package at.shiftcontrol.shiftservice.userdirectory;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.NonNull;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.user.UserSearchDto;

public interface UserDirectoryService {
    DirectoryUser getUserById(String userId);

    Collection<DirectoryUser> getUserByIds(Collection<String> userIds);

    List<DirectoryUser> getAllUsers();

    List<DirectoryUser> getAllAdmins();

    @NonNull PaginationDto<DirectoryUser> searchUsers(int page, int size, @NonNull UserSearchDto searchDto);

    default void invalidateCachedUser(String userId) {
        // Most directory implementations are uncached. LocalUserDirectoryService overrides this.
    }
}

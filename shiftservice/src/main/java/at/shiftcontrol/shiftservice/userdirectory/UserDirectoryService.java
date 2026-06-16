package at.shiftcontrol.shiftservice.userdirectory;

import java.util.Collection;
import java.util.List;

public interface UserDirectoryService {
    DirectoryUser getUserById(String userId);

    Collection<DirectoryUser> getUserByIds(Collection<String> userIds);

    List<DirectoryUser> getAllUsers();

    List<DirectoryUser> getAllAdmins();
}

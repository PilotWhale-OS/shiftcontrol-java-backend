package at.shiftcontrol.shiftservice.auth;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@Service
public class KeycloakUserService implements UserDirectoryService {
    private final Keycloak keycloak;
    private final String realm;

    public KeycloakUserService(Keycloak keycloak) {
        this.keycloak = keycloak;
        this.realm = "dev";
    }

    public DirectoryUser getUserById(String userId) {
        return toDirectoryUser(keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation());
    }

    @Override
    public Collection<DirectoryUser> getUserByIds(Collection<String> userIds) {
        return keycloak
            .realm(realm)
            .users()
            .list()
            .stream()
            .filter(u -> userIds.contains(u.getId()))
            .map(KeycloakUserService::toDirectoryUser)
            .toList();
    }

    @Override
    public List<DirectoryUser> getAllUsers() {
        return keycloak
            .realm(realm)
            .users()
            .list()
            .stream()
            .sorted(Comparator.comparing(user -> Objects.toString(user.getLastName(), "")))
            .map(KeycloakUserService::toDirectoryUser)
            .toList();
    }

    @Override
    public List<DirectoryUser> getAllAdmins() {
        return keycloak
            .realm(realm)
            .users()
            .searchByAttributes("userType:ADMIN")
            .stream()
            .sorted(Comparator.comparing(user -> Objects.toString(user.getLastName(), "")))
            .map(KeycloakUserService::toDirectoryUser)
            .toList();
    }

    public List<DirectoryUser> getAllAssigned() {
        return getAllUsers()
            .stream()
            /* by default users have no userType attribute, therefore it can't be queried via api */
            .filter(user -> user.userType() != UserType.ADMIN)
            .toList();
    }

    private static DirectoryUser toDirectoryUser(UserRepresentation user) {
        var userTypeAttr = user.firstAttribute("userType");
        var userType = userTypeAttr == null ? UserType.ASSIGNED : UserType.valueOf(userTypeAttr);
        return new DirectoryUser(
            user.getId(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            userType
        );
    }
}

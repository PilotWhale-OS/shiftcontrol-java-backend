package at.shiftcontrol.shiftservice.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

@Service
public class KeycloakUserService {
    private final Keycloak keycloak;
    private final String realm;

    public KeycloakUserService(Keycloak keycloak) {
        this.keycloak = keycloak;
        this.realm = "dev";
    }

    public UserRepresentation getUserById(String userId) {
        return keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();
    }

    public Collection<UserRepresentation> getUserByIds(Collection<String> userIds) {
        return keycloak
            .realm(realm)
            .users()
            .list()
            .stream()
            .filter(u -> userIds.contains(u.getId()))
            .toList();
    }

    public List<UserRepresentation> getAllUsers() {
        return keycloak
            .realm(realm)
            .users()
            .list();
    }

    public List<UserRepresentation> getAllAdmins() {
        return keycloak
            .realm(realm)
            .users()
            .searchByAttributes("userType:ADMIN");
    }

    public List<UserRepresentation> getAllAssigned() {
        return keycloak
            .realm(realm)
            .users()
            .searchByAttributes("userType:ASSIGNED");
    }
}

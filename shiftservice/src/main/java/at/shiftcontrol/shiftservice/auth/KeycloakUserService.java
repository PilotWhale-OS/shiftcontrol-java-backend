package at.shiftcontrol.shiftservice.auth;

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
}

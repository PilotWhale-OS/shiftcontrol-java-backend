package at.shiftcontrol.shiftservice.auth;

import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
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

    public UserRepresentation getUserById(Long userId) {
        return keycloak
            .realm(realm)
            .users()
            .get(userId.toString())
            .toRepresentation();
    }

    public UserRepresentation getVolunteerById(Long userId) throws NotFoundException {
        //TODO implement role check
        try {
            return keycloak
                .realm(realm)
                .users()
                .get(userId.toString())
                .toRepresentation();
        } catch (NotFoundException nfe) {
            throw new NotFoundException("Volunteer with ID %d not found".formatted(userId));
        }
    }

    public UserRepresentation getShiftPlanerById(Long userId) throws NotFoundException {
        //TODO implement role check
        try {
            return keycloak
                .realm(realm)
                .users()
                .get(userId.toString())
                .toRepresentation();
        } catch (NotFoundException nfe) {
            throw new NotFoundException("Shiftplaner with ID %d not found".formatted(userId));
        }
    }

    public UserRepresentation getAdminById(Long userId) throws NotFoundException {
        //TODO implement role check
        try {
            return keycloak
                .realm(realm)
                .users()
                .get(userId.toString())
                .toRepresentation();
        } catch (NotFoundException nfe) {
            throw new NotFoundException("Admin with ID %d not found".formatted(userId));
        }
    }

}

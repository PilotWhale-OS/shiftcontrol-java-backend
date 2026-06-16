package at.shiftcontrol.shiftservice.auth;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {
    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Test
    void getUserById_mapsRepresentationAndDefaultsMissingUserTypeToAssigned() {
        var representation = userRepresentation("1", "alice", "Alice", "Anderson", "alice@example.com", null);
        var service = createService();

        when(usersResource.get("1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(representation);

        DirectoryUser user = service.getUserById("1");

        assertThat(user).isEqualTo(new DirectoryUser(
            "1",
            "alice",
            "Alice",
            "Anderson",
            "alice@example.com",
            UserType.ASSIGNED
        ));
        verify(usersResource).get("1");
    }

    @Test
    void getAllAdmins_usesAttributeSearchAndReturnsSortedAdmins() {
        var zeta = userRepresentation("2", "zoe", "Zoe", "Zimmer", "zoe@example.com", "ADMIN");
        var alpha = userRepresentation("1", "adam", "Adam", "Able", "adam@example.com", "ADMIN");
        var service = createService();

        when(usersResource.searchByAttributes("userType:ADMIN")).thenReturn(List.of(zeta, alpha));

        var admins = service.getAllAdmins();

        assertThat(admins)
            .extracting(DirectoryUser::id)
            .containsExactly("1", "2");
    }

    @Test
    void getAllAssigned_filtersOutAdminsAfterDirectoryMapping() {
        var assigned = userRepresentation("1", "alice", "Alice", "Anderson", "alice@example.com", null);
        var admin = userRepresentation("2", "admin", "Admin", "User", "admin@example.com", "ADMIN");
        var service = createService();

        when(usersResource.list()).thenReturn(List.of(admin, assigned));

        var assignedUsers = service.getAllAssigned();

        assertThat(assignedUsers)
            .extracting(DirectoryUser::id)
            .containsExactly("1");
    }

    @Test
    void getUserByIds_onlyReturnsRequestedUsers() {
        var alice = userRepresentation("1", "alice", "Alice", "Anderson", "alice@example.com", null);
        var bob = userRepresentation("2", "bob", "Bob", "Builder", "bob@example.com", "ASSIGNED");
        var charlie = userRepresentation("3", "charlie", "Charlie", "Chaplin", "charlie@example.com", "ASSIGNED");
        var service = createService();

        when(usersResource.list()).thenReturn(List.of(alice, bob, charlie));

        var users = service.getUserByIds(List.of("1", "3"));

        assertThat(users)
            .extracting(DirectoryUser::id)
            .containsExactly("1", "3");
    }

    private KeycloakUserService createService() {
        when(keycloak.realm("dev")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        return new KeycloakUserService(keycloak);
    }

    private static UserRepresentation userRepresentation(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        String userType
    ) {
        var user = new UserRepresentation();
        user.setId(id);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        if (userType != null) {
            user.singleAttribute("userType", userType);
        }
        return user;
    }
}

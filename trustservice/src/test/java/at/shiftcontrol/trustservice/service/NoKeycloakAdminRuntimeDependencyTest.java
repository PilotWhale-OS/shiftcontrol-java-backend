package at.shiftcontrol.trustservice.service;

import org.springframework.util.ClassUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoKeycloakAdminRuntimeDependencyTest {
    @Test
    void keycloakAdminClientIsNotAvailableOnTrustserviceClasspath() {
        assertThat(ClassUtils.isPresent("org.keycloak.admin.client.Keycloak", getClass().getClassLoader())).isFalse();
        assertThat(ClassUtils.isPresent("org.keycloak.representations.idm.UserRepresentation", getClass().getClassLoader())).isFalse();
    }
}

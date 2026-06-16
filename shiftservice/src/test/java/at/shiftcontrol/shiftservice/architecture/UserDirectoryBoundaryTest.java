package at.shiftcontrol.shiftservice.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "at.shiftcontrol.shiftservice")
class UserDirectoryBoundaryTest {
    @ArchTest
    static final ArchRule keycloak_adapter_is_not_used_directly_outside_auth =
        noClasses()
            .that()
            .resideOutsideOfPackages(
                "..auth..",
                "..architecture.."
            )
            .should()
            .dependOnClassesThat()
            .haveSimpleName("KeycloakUserService");

    @ArchTest
    static final ArchRule keycloak_user_representation_is_confined_to_auth_adapter =
        noClasses()
            .that()
            .resideOutsideOfPackages(
                "..auth..",
                "..architecture.."
            )
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("org.keycloak.representations.idm.UserRepresentation");
}

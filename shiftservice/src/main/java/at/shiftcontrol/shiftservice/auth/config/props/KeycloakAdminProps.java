package at.shiftcontrol.shiftservice.auth.config.props;

public record KeycloakAdminProps(
    String id,
    String realm,
    String username,
    String password
) {
}

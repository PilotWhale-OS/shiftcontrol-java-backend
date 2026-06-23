package at.shiftcontrol.shiftservice.auth.config.props;

import java.util.Set;

public record OidcProviderProps(
    String issuerUri,
    String jwkSetUri,
    Set<String> allowedIssuers
) {}

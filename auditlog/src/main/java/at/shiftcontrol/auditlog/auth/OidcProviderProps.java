package at.shiftcontrol.auditlog.auth;

import java.util.Set;

public record OidcProviderProps(
    String issuerUri,
    String jwkSetUri,
    Set<String> allowedIssuers
) {}

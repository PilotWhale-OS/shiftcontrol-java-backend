package at.shiftcontrol.shiftservice.auth;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.lib.exception.IllegalArgumentException;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;

@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationUserManager {
    private Cache<String, ShiftControlUser> userCache;
    private final UserAttributeProvider userAttributeProvider;

    @PostConstruct
    public void init() {
        userCache = Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterAccess(Duration.ofMinutes(20))
            .build();
    }

    public ApplicationUser getOrCreateUser(Jwt jwt) {
        var userTypeString = jwt.getClaimAsString("userType");
        var userId = jwt.getClaimAsString("sub");
        if (userId == null) {
            throw new IllegalArgumentException("User token does not contain 'sub' claim");
        }

        Set<String> scopeAuthorities = extractAuthorities(jwt);
        String username = firstNonBlank(jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("client_id"), userId);

        if (isMachineToken(jwt, scopeAuthorities)) {
            log.debug("Creating service ApplicationUser for username {} and userId {}", username, userId);
            return getOrCreateServiceUser(userId, username, scopeAuthorities);
        }

        UserType userType = Objects.equals(userTypeString, UserType.ADMIN.name())
            || scopeAuthorities.contains(Authorities.PLATFORM_ADMIN)
            ? UserType.ADMIN
            : UserType.ASSIGNED;
        log.debug("Creating human ApplicationUser of type {} for username {} and userId {}", userType, username, userId);
        return getOrCreateHumanUser(userId, userType, username, scopeAuthorities);
    }

    private ShiftControlUser getOrCreateHumanUser(String userId, UserType userType, String username, Set<String> scopeAuthorities) {
        var user = userCache.getIfPresent(userId);
        if (user == null) {
            user = ApplicationUserFactory.createHumanUser(userType, username, userId, scopeAuthorities, userAttributeProvider);
            userCache.put(userId, user);
        }

        return user;
    }

    private ShiftControlUser getOrCreateServiceUser(String userId, String username, Set<String> scopeAuthorities) {
        String cacheKey = "service:" + userId + ":" + String.join(",", scopeAuthorities);
        var user = userCache.getIfPresent(cacheKey);
        if (user == null) {
            user = ApplicationUserFactory.createServiceUser(username, userId, scopeAuthorities);
            userCache.put(cacheKey, user);
        }

        return user;
    }

    private Set<String> extractAuthorities(Jwt jwt) {
        LinkedHashSet<String> authorities = new LinkedHashSet<>();
        authorities.addAll(extractScopeValues(jwt.getClaimAsString("scope")));

        Object scpClaim = jwt.getClaims().get("scp");
        if (scpClaim instanceof Collection<?> scopeCollection) {
            scopeCollection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .forEach(authorities::add);
        } else if (scpClaim instanceof String scopeString) {
            authorities.addAll(extractScopeValues(scopeString));
        }

        return authorities;
    }

    private Set<String> extractScopeValues(String scopeString) {
        if (scopeString == null || scopeString.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(scopeString.split("\\s+"))
            .filter(scope -> !scope.isBlank())
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isMachineToken(Jwt jwt, Set<String> scopeAuthorities) {
        if (scopeAuthorities.contains("openid")) {
            return false;
        }

        return Stream.of(
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("userType")
            )
            .allMatch(value -> value == null || value.isBlank());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}

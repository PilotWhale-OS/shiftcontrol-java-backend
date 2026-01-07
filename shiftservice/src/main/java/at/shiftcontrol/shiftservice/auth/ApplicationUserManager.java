package at.shiftcontrol.shiftservice.auth;

import java.time.Duration;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;

@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationUserManager {
    private Cache userCache;
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
        var userType = userTypeString == null ? UserType.ASSIGNED : UserType.valueOf(userTypeString);
        var username = jwt.getClaimAsString("preferred_username");
        var userId = jwt.getClaimAsString("sub");
        log.debug("Creating ApplicationUser of type {} for username {} and userId {}", userType, username, userId);

        if (userId == null) {
            throw new IllegalArgumentException("User token does not contain 'sub' claim");
        }

        return getOrCreateUserInternal(userId, userType, username);
    }

    private ShiftControlUser getOrCreateUserInternal(String userId, UserType userType, String username) {
        var user = (ShiftControlUser) userCache.getIfPresent(userId);
        if (user == null) {
            user = ApplicationUserFactory.createUser(userType, username, userId, userAttributeProvider);
            userCache.put(userId, user);
        }

        return user;
    }
}

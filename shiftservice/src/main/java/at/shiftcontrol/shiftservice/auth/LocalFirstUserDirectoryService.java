package at.shiftcontrol.shiftservice.auth;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.ExternalIdentity;
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@Service
@Primary
@RequiredArgsConstructor
public class LocalFirstUserDirectoryService implements UserDirectoryService {
    private static final String ADMIN_CACHE_KEY = "all-admins";

    private final ExternalIdentityRepository externalIdentityRepository;
    private final KeycloakUserService keycloakUserService;

    private Cache<String, DirectoryUser> userCache;
    private Cache<String, List<DirectoryUser>> listCache;

    @PostConstruct
    public void init() {
        userCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();
        listCache = Caffeine.newBuilder()
            .maximumSize(16)
            .expireAfterWrite(Duration.ofMinutes(2))
            .build();
    }

    @Override
    public DirectoryUser getUserById(String userId) {
        DirectoryUser cachedUser = userCache.getIfPresent(userId);
        if (cachedUser != null) {
            return cachedUser;
        }

        DirectoryUser resolvedUser = resolveLocalUsers(Set.of(userId)).get(userId);
        if (resolvedUser == null) {
            resolvedUser = keycloakUserService.getUserById(userId);
        }

        userCache.put(userId, resolvedUser);
        return resolvedUser;
    }

    @Override
    public Collection<DirectoryUser> getUserByIds(Collection<String> userIds) {
        Set<String> requestedIds = new LinkedHashSet<>(userIds);
        Map<String, DirectoryUser> resolvedUsers = new LinkedHashMap<>();
        Set<String> missingIds = new LinkedHashSet<>();

        for (String userId : requestedIds) {
            DirectoryUser cachedUser = userCache.getIfPresent(userId);
            if (cachedUser != null) {
                resolvedUsers.put(userId, cachedUser);
            } else {
                missingIds.add(userId);
            }
        }

        if (!missingIds.isEmpty()) {
            Map<String, DirectoryUser> localUsers = resolveLocalUsers(missingIds);
            localUsers.values().forEach(user -> userCache.put(user.id(), user));
            resolvedUsers.putAll(localUsers);
            missingIds.removeAll(localUsers.keySet());
        }

        if (!missingIds.isEmpty()) {
            Collection<DirectoryUser> fallbackUsers = keycloakUserService.getUserByIds(missingIds);
            fallbackUsers.forEach(user -> {
                userCache.put(user.id(), user);
                resolvedUsers.put(user.id(), user);
            });
        }

        return requestedIds.stream()
            .map(resolvedUsers::get)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    @Override
    public List<DirectoryUser> getAllUsers() {
        return keycloakUserService.getAllUsers();
    }

    @Override
    public List<DirectoryUser> getAllAdmins() {
        List<DirectoryUser> cachedAdmins = listCache.getIfPresent(ADMIN_CACHE_KEY);
        if (cachedAdmins != null) {
            return cachedAdmins;
        }

        List<DirectoryUser> admins = keycloakUserService.getAllAdmins();
        listCache.put(ADMIN_CACHE_KEY, admins);
        admins.forEach(admin -> userCache.put(admin.id(), admin));
        return admins;
    }

    private Map<String, DirectoryUser> resolveLocalUsers(Collection<String> userIds) {
        return externalIdentityRepository.findAllBySubjectIn(userIds).stream()
            .collect(Collectors.groupingBy(
                ExternalIdentity::getSubject,
                LinkedHashMap::new,
                Collectors.collectingAndThen(Collectors.toList(), this::toPreferredDirectoryUser)
            ));
    }

    private DirectoryUser toPreferredDirectoryUser(List<ExternalIdentity> externalIdentities) {
        ExternalIdentity preferredExternalIdentity = externalIdentities.stream()
            .max(Comparator.comparing(ExternalIdentity::getLastSeenAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(externalIdentities.get(0));

        var userAccount = preferredExternalIdentity.getUserAccount();
        return new DirectoryUser(
            preferredExternalIdentity.getSubject(),
            firstNonBlank(userAccount.getPreferredUsername(), preferredExternalIdentity.getSubject()),
            firstNonBlank(userAccount.getFirstName(), ""),
            firstNonBlank(userAccount.getLastName(), ""),
            firstNonBlank(userAccount.getEmail(), ""),
            UserType.ASSIGNED
        );
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

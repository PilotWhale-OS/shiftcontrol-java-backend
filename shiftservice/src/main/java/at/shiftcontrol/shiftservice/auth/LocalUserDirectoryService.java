package at.shiftcontrol.shiftservice.auth;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.ExternalIdentity;
import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserAccountRepository;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.LocalUserDirectoryProvisioningService;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@Service
@Primary
@RequiredArgsConstructor
public class LocalUserDirectoryService implements UserDirectoryService {
    private static final String ADMIN_CACHE_KEY = "all-admins";

    private final ExternalIdentityRepository externalIdentityRepository;
    private final UserAccountRepository userAccountRepository;
    private final VolunteerRepository volunteerRepository;
    private final LocalUserDirectoryProvisioningService provisioningService;

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
        if (resolvedUser == null && volunteerRepository.existsById(userId)) {
            provisioningService.ensureUserAccountForVolunteerId(userId);
            resolvedUser = resolveLocalUsers(Set.of(userId)).get(userId);
        }
        if (resolvedUser == null) {
            throw new BadRequestException("User not found: " + userId);
        }

        userCache.put(userId, resolvedUser);
        return resolvedUser;
    }

    @Override
    public Collection<DirectoryUser> getUserByIds(Collection<String> userIds) {
        Set<String> requestedIds = userIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (requestedIds.isEmpty()) {
            return List.of();
        }

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
            provisioningService.ensureUserAccountsForVolunteerIds(missingIds);
            Map<String, DirectoryUser> provisionedUsers = resolveLocalUsers(missingIds);
            provisionedUsers.values().forEach(user -> userCache.put(user.id(), user));
            resolvedUsers.putAll(provisionedUsers);
        }

        return requestedIds.stream()
            .map(resolvedUsers::get)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public List<DirectoryUser> getAllUsers() {
        provisioningService.ensureUserAccountsForVolunteerIds(
            volunteerRepository.findAll().stream().map(at.shiftcontrol.lib.entity.Volunteer::getId).toList()
        );

        List<DirectoryUser> users = userAccountRepository.findAllWithExternalIdentities().stream()
            .map(this::toDirectoryUser)
            .sorted(Comparator
                .comparing((DirectoryUser user) -> firstNonBlankOrEmpty(user.lastName(), ""))
                .thenComparing(user -> firstNonBlankOrEmpty(user.firstName(), ""))
                .thenComparing(user -> firstNonBlankOrEmpty(user.username(), user.id())))
            .toList();
        users.forEach(user -> userCache.put(user.id(), user));
        return users;
    }

    @Override
    public List<DirectoryUser> getAllAdmins() {
        List<DirectoryUser> cachedAdmins = listCache.getIfPresent(ADMIN_CACHE_KEY);
        if (cachedAdmins != null) {
            return cachedAdmins;
        }

        List<DirectoryUser> admins = userAccountRepository.findAllPlatformAdminsWithExternalIdentities().stream()
            .map(this::toDirectoryUser)
            .sorted(Comparator
                .comparing((DirectoryUser user) -> firstNonBlankOrEmpty(user.lastName(), ""))
                .thenComparing(user -> firstNonBlankOrEmpty(user.firstName(), ""))
                .thenComparing(user -> firstNonBlankOrEmpty(user.username(), user.id())))
            .toList();
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

    private DirectoryUser toDirectoryUser(UserAccount userAccount) {
        ExternalIdentity preferredExternalIdentity = userAccount.getExternalIdentities().stream()
            .max(Comparator.comparing(ExternalIdentity::getLastSeenAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .orElseThrow(() -> new IllegalStateException("User account has no external identities: " + userAccount.getId()));

        return new DirectoryUser(
            preferredExternalIdentity.getSubject(),
            firstNonBlank(userAccount.getPreferredUsername(), preferredExternalIdentity.getSubject()),
            firstNonBlank(userAccount.getFirstName(), ""),
            firstNonBlank(userAccount.getLastName(), ""),
            firstNonBlank(userAccount.getEmail(), ""),
            userAccount.isPlatformAdmin() ? UserType.ADMIN : UserType.ASSIGNED
        );
    }

    private DirectoryUser toPreferredDirectoryUser(List<ExternalIdentity> externalIdentities) {
        ExternalIdentity preferredExternalIdentity = externalIdentities.stream()
            .max(Comparator.comparing(ExternalIdentity::getLastSeenAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(externalIdentities.get(0));

        UserAccount userAccount = preferredExternalIdentity.getUserAccount();
        return new DirectoryUser(
            preferredExternalIdentity.getSubject(),
            firstNonBlank(userAccount.getPreferredUsername(), preferredExternalIdentity.getSubject()),
            firstNonBlank(userAccount.getFirstName(), ""),
            firstNonBlank(userAccount.getLastName(), ""),
            firstNonBlank(userAccount.getEmail(), ""),
            userAccount.isPlatformAdmin() ? UserType.ADMIN : UserType.ASSIGNED
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

    private String firstNonBlankOrEmpty(String... values) {
        String value = firstNonBlank(values);
        return value != null ? value : "";
    }
}

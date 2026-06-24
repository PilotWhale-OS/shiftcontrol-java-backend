package at.shiftcontrol.shiftservice.userdirectory.current;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.ExternalIdentity;
import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.type.UserAccountStatus;
import at.shiftcontrol.lib.type.UserProfileSource;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.ExternalIdentityRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserAccountRepository;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteClaimService;
import at.shiftcontrol.shiftservice.userdirectory.LocalUserDirectoryProvisioningService;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@Service
@RequiredArgsConstructor
public class CurrentUserProfileSyncService {
    private static final Duration PROFILE_SYNC_TTL = Duration.ofMinutes(5);

    private final CurrentSubjectProfileResolver currentSubjectProfileResolver;
    private final UserAccountRepository userAccountRepository;
    private final ExternalIdentityRepository externalIdentityRepository;
    private final VolunteerRepository volunteerRepository;
    private final UserInviteClaimService userInviteClaimService;
    private final UserDirectoryService userDirectoryService;

    @Transactional
    public CurrentSubjectProfileSyncResult syncCurrentSubject() {
        CurrentSubjectProfile currentSubjectProfile = currentSubjectProfileResolver.resolveCurrentSubject();
        Instant now = Instant.now();
        ExternalIdentity existingExternalIdentity = externalIdentityRepository.findByIssuerAndSubject(
            currentSubjectProfile.issuer(),
            currentSubjectProfile.subject()
        ).orElse(null);
        if (existingExternalIdentity == null && !shouldPersistLocalState(currentSubjectProfile)) {
            return new CurrentSubjectProfileSyncResult(currentSubjectProfile, buildTransientUserAccount(currentSubjectProfile, now));
        }

        return syncCurrentSubject(currentSubjectProfile, now);
    }

    @Transactional
    public CurrentSubjectProfileSyncResult syncCurrentSubjectIfStale() {
        CurrentSubjectProfile currentSubjectProfile = currentSubjectProfileResolver.resolveCurrentSubject();
        Instant now = Instant.now();
        ExternalIdentity existingExternalIdentity = externalIdentityRepository.findByIssuerAndSubject(
            currentSubjectProfile.issuer(),
            currentSubjectProfile.subject()
        ).orElse(null);

        if (existingExternalIdentity == null && !shouldPersistLocalState(currentSubjectProfile)) {
            return new CurrentSubjectProfileSyncResult(currentSubjectProfile, buildTransientUserAccount(currentSubjectProfile, now));
        }

        if (existingExternalIdentity != null && !shouldRefresh(existingExternalIdentity.getUserAccount(), currentSubjectProfile, now)) {
            userInviteClaimService.claimPendingInvites(
                existingExternalIdentity.getUserAccount(),
                currentSubjectProfile.subject(),
                currentSubjectProfile.email(),
                now
            );
            return new CurrentSubjectProfileSyncResult(currentSubjectProfile, existingExternalIdentity.getUserAccount());
        }

        return syncCurrentSubject(currentSubjectProfile, now);
    }

    private CurrentSubjectProfileSyncResult syncCurrentSubject(CurrentSubjectProfile currentSubjectProfile, Instant now) {
        ExternalIdentity existingExternalIdentity = externalIdentityRepository.findByIssuerAndSubject(
            currentSubjectProfile.issuer(),
            currentSubjectProfile.subject()
        ).orElse(null);

        UserAccount userAccount;
        if (existingExternalIdentity == null) {
            userAccount = findExistingPlaceholderUserAccount(currentSubjectProfile.subject());
            if (userAccount == null) {
                userAccount = UserAccount.builder()
                    .status(UserAccountStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            }
            applyCurrentProfile(userAccount, currentSubjectProfile, now);
            userAccount.addExternalIdentity(ExternalIdentity.builder()
                .issuer(currentSubjectProfile.issuer())
                .subject(currentSubjectProfile.subject())
                .createdAt(now)
                .lastSeenAt(now)
                .build());
            userAccount = userAccountRepository.save(userAccount);
        } else {
            userAccount = existingExternalIdentity.getUserAccount();
            applyCurrentProfile(userAccount, currentSubjectProfile, now);
            existingExternalIdentity.setLastSeenAt(now);
            userAccount = userAccountRepository.save(userAccount);
            externalIdentityRepository.save(existingExternalIdentity);
        }

        userInviteClaimService.claimPendingInvites(
            userAccount,
            currentSubjectProfile.subject(),
            currentSubjectProfile.email(),
            now
        );
        userDirectoryService.invalidateCachedUser(currentSubjectProfile.subject());
        return new CurrentSubjectProfileSyncResult(currentSubjectProfile, userAccount);
    }

    private boolean shouldPersistLocalState(CurrentSubjectProfile currentSubjectProfile) {
        if (currentSubjectProfile.isPlatformAdmin()) {
            return true;
        }
        if (volunteerRepository.existsById(currentSubjectProfile.subject())) {
            return true;
        }

        String email = currentSubjectProfile.email();
        return email != null
            && !email.isBlank()
            && userInviteClaimService.hasPendingInviteForEmail(email);
    }

    private UserAccount buildTransientUserAccount(CurrentSubjectProfile currentSubjectProfile, Instant now) {
        UserAccount userAccount = UserAccount.builder()
            .status(UserAccountStatus.ACTIVE)
            .platformAdmin(currentSubjectProfile.isPlatformAdmin())
            .lastLoginAt(now)
            .lastProfileSyncAt(now)
            .lastProfileSyncSource(UserProfileSource.TOKEN_CLAIMS)
            .createdAt(now)
            .updatedAt(now)
            .build();
        applyCurrentProfile(userAccount, currentSubjectProfile, now);
        return userAccount;
    }

    private boolean shouldRefresh(UserAccount userAccount, CurrentSubjectProfile currentSubjectProfile, Instant now) {
        if (userAccount.getLastProfileSyncAt() == null) {
            return true;
        }
        if (userAccount.getLastProfileSyncAt().isBefore(now.minus(PROFILE_SYNC_TTL))) {
            return true;
        }

        return valueChanged(currentSubjectProfile.preferredUsername(), userAccount.getPreferredUsername())
            || valueChanged(currentSubjectProfile.firstName(), userAccount.getFirstName())
            || valueChanged(currentSubjectProfile.lastName(), userAccount.getLastName())
            || valueChanged(currentSubjectProfile.email(), userAccount.getEmail())
            || valueChanged(currentSubjectProfile.profile(), userAccount.getProfile())
            || (currentSubjectProfile.emailVerified() != null && currentSubjectProfile.emailVerified() != userAccount.isEmailVerified());
    }

    private void applyCurrentProfile(UserAccount userAccount, CurrentSubjectProfile currentSubjectProfile, Instant now) {
        userAccount.setPreferredUsername(firstNonBlank(currentSubjectProfile.preferredUsername(), userAccount.getPreferredUsername()));
        userAccount.setFirstName(firstNonBlank(currentSubjectProfile.firstName(), userAccount.getFirstName()));
        userAccount.setLastName(firstNonBlank(currentSubjectProfile.lastName(), userAccount.getLastName()));
        userAccount.setEmail(firstNonBlank(currentSubjectProfile.email(), userAccount.getEmail()));
        userAccount.setProfile(firstNonBlank(currentSubjectProfile.profile(), userAccount.getProfile()));
        userAccount.setDisplayName(buildDisplayName(
            userAccount.getFirstName(),
            userAccount.getLastName(),
            userAccount.getPreferredUsername(),
            userAccount.getEmail(),
            currentSubjectProfile.subject()
        ));
        if (currentSubjectProfile.emailVerified() != null) {
            userAccount.setEmailVerified(currentSubjectProfile.emailVerified());
        }
        if (userAccount.getStatus() == UserAccountStatus.PENDING_INVITE) {
            userAccount.setStatus(UserAccountStatus.ACTIVE);
        }
        userAccount.setPlatformAdmin(currentSubjectProfile.isPlatformAdmin());
        userAccount.setLastLoginAt(now);
        userAccount.setLastProfileSyncAt(now);
        userAccount.setLastProfileSyncSource(UserProfileSource.TOKEN_CLAIMS);
        userAccount.setUpdatedAt(now);
    }

    private UserAccount findExistingPlaceholderUserAccount(String subject) {
        return externalIdentityRepository.findByIssuerAndSubject(LocalUserDirectoryProvisioningService.LEGACY_VOLUNTEER_ISSUER, subject)
            .map(ExternalIdentity::getUserAccount)
            .filter(Objects::nonNull)
            .orElse(null);
    }

    private String buildDisplayName(String firstName, String lastName, String preferredUsername, String email, String subject) {
        String name = joinNonBlank(firstName, lastName);
        if (name != null) {
            return name;
        }

        return firstNonBlank(preferredUsername, email, subject);
    }

    private String joinNonBlank(String firstValue, String secondValue) {
        boolean hasFirst = firstValue != null && !firstValue.isBlank();
        boolean hasSecond = secondValue != null && !secondValue.isBlank();
        if (!hasFirst && !hasSecond) {
            return null;
        }
        if (!hasFirst) {
            return secondValue;
        }
        if (!hasSecond) {
            return firstValue;
        }

        return firstValue + " " + secondValue;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private boolean valueChanged(String currentValue, String persistedValue) {
        if (currentValue == null || currentValue.isBlank()) {
            return false;
        }

        return !Objects.equals(currentValue, persistedValue);
    }
}

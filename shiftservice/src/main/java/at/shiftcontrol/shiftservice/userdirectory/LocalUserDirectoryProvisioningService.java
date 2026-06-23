package at.shiftcontrol.shiftservice.userdirectory;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
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

@Service
@RequiredArgsConstructor
public class LocalUserDirectoryProvisioningService {
    public static final String LEGACY_VOLUNTEER_ISSUER = "urn:shiftservice:legacy-volunteer";

    private final VolunteerRepository volunteerRepository;
    private final UserAccountRepository userAccountRepository;
    private final ExternalIdentityRepository externalIdentityRepository;

    @Transactional
    public void ensureUserAccountForVolunteerId(String volunteerId) {
        if (!externalIdentityRepository.findAllBySubject(volunteerId).isEmpty()) {
            return;
        }
        if (!volunteerRepository.existsById(volunteerId)) {
            return;
        }

        createPlaceholderUserAccount(volunteerId);
    }

    @Transactional
    public void ensureUserAccountsForVolunteerIds(Collection<String> volunteerIds) {
        Set<String> requestedVolunteerIds = volunteerIds.stream()
            .filter(id -> id != null && !id.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (requestedVolunteerIds.isEmpty()) {
            return;
        }

        Set<String> existingSubjects = externalIdentityRepository.findAllBySubjectIn(requestedVolunteerIds).stream()
            .map(ExternalIdentity::getSubject)
            .collect(Collectors.toSet());

        volunteerRepository.findAllById(requestedVolunteerIds).stream()
            .map(at.shiftcontrol.lib.entity.Volunteer::getId)
            .filter(volunteerId -> !existingSubjects.contains(volunteerId))
            .forEach(this::createPlaceholderUserAccount);
    }

    private void createPlaceholderUserAccount(String volunteerId) {
        Instant now = Instant.now();
        UserAccount userAccount = UserAccount.builder()
            .status(UserAccountStatus.ACTIVE)
            .preferredUsername(volunteerId)
            .displayName(volunteerId)
            .emailVerified(false)
            .platformAdmin(false)
            .lastProfileSyncAt(now)
            .lastProfileSyncSource(UserProfileSource.MIGRATION)
            .createdAt(now)
            .updatedAt(now)
            .build();
        userAccount.addExternalIdentity(ExternalIdentity.builder()
            .issuer(LEGACY_VOLUNTEER_ISSUER)
            .subject(volunteerId)
            .createdAt(now)
            .lastSeenAt(now)
            .build());

        try {
            userAccountRepository.save(userAccount);
        } catch (DataIntegrityViolationException ignored) {
            // Another request created the same placeholder or linked identity in the meantime.
        }
    }
}

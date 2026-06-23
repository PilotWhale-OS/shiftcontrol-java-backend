package at.shiftcontrol.shiftservice.service.userdirectory;

import java.time.Instant;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.UserAccount;
import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType;
import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserInviteRepository;

@Service
@RequiredArgsConstructor
public class UserInviteClaimService {
    private Cache<String, Boolean> pendingInviteEmailCache;

    private final UserInviteRepository userInviteRepository;
    private final VolunteerRepository volunteerRepository;
    private final UserAttributeProvider userAttributeProvider;

    @PostConstruct
    public void init() {
        pendingInviteEmailCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(java.time.Duration.ofMinutes(2))
            .build();
    }

    @Transactional(readOnly = true)
    public boolean hasPendingInviteForEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }

        return pendingInviteEmailCache.get(normalizedEmail, this::loadHasPendingInviteForEmail);
    }

    public void invalidatePendingInviteEmailCache(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail != null) {
            pendingInviteEmailCache.invalidate(normalizedEmail);
        }
    }

    @Transactional
    public void claimPendingInvites(UserAccount userAccount, String userId, String email, Instant now) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return;
        }

        var pendingInvites = userInviteRepository.findAllByEmailIgnoreCaseAndStatus(normalizedEmail, UserInviteStatus.PENDING);
        if (pendingInvites.isEmpty()) {
            pendingInviteEmailCache.put(normalizedEmail, false);
            return;
        }

        Volunteer volunteer = null;
        boolean volunteerChanged = false;

        for (UserInvite invite : pendingInvites) {
            if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(now)) {
                invite.setStatus(UserInviteStatus.EXPIRED);
                continue;
            }

            if (volunteer == null) {
                volunteer = volunteerRepository.findById(userId).orElseGet(() -> createVolunteer(userId));
            }

            volunteerChanged |= applyInvite(invite, volunteer);
            invite.setClaimedUserAccount(userAccount);
            invite.setClaimedAt(now);
            invite.setStatus(UserInviteStatus.CLAIMED);
        }

        if (volunteer != null && volunteerChanged) {
            volunteerRepository.save(volunteer);
            userAttributeProvider.invalidateUserCache(userId);
        }
        pendingInviteEmailCache.put(normalizedEmail, false);
    }

    private boolean applyInvite(UserInvite invite, Volunteer volunteer) {
        boolean changed = false;

        if (invite.getPendingShiftPlanAccesses() != null) {
            for (var access : invite.getPendingShiftPlanAccesses()) {
                ShiftPlan shiftPlan = access.getShiftPlan();
                changed |= ensureVolunteerAccess(volunteer, shiftPlan);
                if (access.getAccessType() == UserInviteShiftPlanAccessType.PLANNER) {
                    changed |= volunteer.getPlanningPlans().add(shiftPlan);
                }
                if (access.getAccessType() == UserInviteShiftPlanAccessType.LOCKED) {
                    changed |= volunteer.getLockedPlans().add(shiftPlan);
                }
            }
        }

        if (invite.getPendingRoles() != null) {
            changed |= volunteer.getRoles().addAll(invite.getPendingRoles());
        }

        return changed;
    }

    private boolean ensureVolunteerAccess(Volunteer volunteer, ShiftPlan shiftPlan) {
        return volunteer.getVolunteeringPlans().add(shiftPlan);
    }

    private Volunteer createVolunteer(String userId) {
        Volunteer volunteer = Volunteer.builder()
            .id(userId)
            .planningPlans(new HashSet<>())
            .volunteeringPlans(new HashSet<>())
            .lockedPlans(new HashSet<>())
            .roles(new HashSet<>())
            .notificationSettings(new HashSet<>())
            .build();
        return volunteerRepository.save(volunteer);
    }

    private boolean loadHasPendingInviteForEmail(String normalizedEmail) {
        Instant now = Instant.now();
        return userInviteRepository.findAllByEmailIgnoreCaseAndStatus(normalizedEmail, UserInviteStatus.PENDING).stream()
            .anyMatch(invite -> invite.getExpiresAt() == null || !invite.getExpiresAt().isBefore(now));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalizedEmail = email.trim();
        return normalizedEmail.isBlank() ? null : normalizedEmail;
    }
}

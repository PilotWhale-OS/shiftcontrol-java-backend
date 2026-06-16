package at.shiftcontrol.lib.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import at.shiftcontrol.lib.type.UserInviteStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_invite")
public class UserInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @NotNull
    @Size(max = 320)
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Size(max = 255)
    @Column(name = "preferred_username", length = 255)
    private String preferredUsername;

    @Size(max = 255)
    @Column(name = "first_name", length = 255)
    private String firstName;

    @Size(max = 255)
    @Column(name = "last_name", length = 255)
    private String lastName;

    @Size(max = 255)
    @Column(name = "display_name", length = 255)
    private String displayName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserInviteStatus status;

    @ManyToOne
    @JoinColumn(name = "claimed_user_account_id")
    private UserAccount claimedUserAccount;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "user_invite_role",
        joinColumns = @JoinColumn(name = "invite_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> pendingRoles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "userInvite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserInviteShiftPlanAccess> pendingShiftPlanAccesses = new ArrayList<>();

    public void addPendingShiftPlanAccess(UserInviteShiftPlanAccess pendingShiftPlanAccess) {
        pendingShiftPlanAccess.setUserInvite(this);
        pendingShiftPlanAccesses.add(pendingShiftPlanAccess);
    }
}

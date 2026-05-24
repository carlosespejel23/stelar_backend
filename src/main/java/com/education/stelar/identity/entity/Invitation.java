package com.education.stelar.identity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invited_email", nullable = false, length = 200)
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "invited_by_user_id", nullable = false)
    private UUID invitedByUserId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public static Invitation create(String invitedEmail, InvitationType type,
                                    String token, UUID roleId, UUID invitedByUserId, Instant expiresAt) {
        Invitation inv = new Invitation();
        inv.invitedEmail = invitedEmail.toLowerCase().trim();
        inv.type = type;
        inv.token = token;
        inv.roleId = roleId;
        inv.invitedByUserId = invitedByUserId;
        inv.expiresAt = expiresAt;
        return inv;
    }

    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
    }

    public void revoke() {
        this.status = InvitationStatus.REVOKED;
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}

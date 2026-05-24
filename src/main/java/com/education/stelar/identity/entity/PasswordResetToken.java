package com.education.stelar.identity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public static PasswordResetToken create(String token, UUID userId, String email, Instant expiresAt) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.token = token;
        prt.userId = userId;
        prt.email = email.toLowerCase().trim();
        prt.createdAt = Instant.now();
        prt.expiresAt = expiresAt;
        return prt;
    }

    public void markUsed() {
        this.used = true;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}

package com.education.stelar.identity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.AuditableEntity;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 200, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "profession", length = 10)
    private Profession profession;

    public static User create(String firstName, String lastName, String email, String encodedPassword, Profession profession) {
        User user = new User();
        user.firstName = firstName.trim();
        user.lastName = lastName.trim();
        user.email = email.toLowerCase().trim();
        user.passwordHash = encodedPassword;
        user.profession = profession;
        return user;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void changePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    public void updateProfile(String firstName, String lastName, String profilePictureUrl, Profession profession) {
        if (firstName != null && !firstName.isBlank()) this.firstName = firstName.trim();
        if (lastName != null && !lastName.isBlank()) this.lastName = lastName.trim();
        if (profilePictureUrl != null) this.profilePictureUrl = profilePictureUrl.trim();
        if (profession != null) this.profession = profession;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

package com.education.stelar.identity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.AuditableEntity;

@Entity
@Table(name = "tenants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "owner_id")
    private UUID ownerId;

    public static Tenant create(String name, String slug) {
        Tenant tenant = new Tenant();
        tenant.name = name.trim();
        tenant.slug = slug.toLowerCase().trim();
        return tenant;
    }

    public void update(String name, String description, String logoUrl) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (description != null) this.description = description.trim();
        if (logoUrl != null) this.logoUrl = logoUrl.trim();
    }

    public void assignOwner(UUID userId) {
        this.ownerId = userId;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}

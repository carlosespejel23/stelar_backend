package com.education.stelar.identity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "user_tenants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tenant_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTenant extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private boolean active = false;

    public static UserTenant create(User user, Role role) {
        UserTenant ut = new UserTenant();
        ut.user = user;
        ut.role = role;
        ut.active = false;
        return ut;
    }

    public static UserTenant createActive(User user, Role role) {
        UserTenant ut = new UserTenant();
        ut.user = user;
        ut.role = role;
        ut.active = true;
        return ut;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void assignRole(Role role) {
        this.role = role;
    }

    public String getRoleName() {
        return role != null ? role.getName() : null;
    }

    public UUID getRoleId() {
        return role != null ? role.getId() : null;
    }
}

package com.education.stelar.identity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(name = "system_role", nullable = false)
    private boolean systemRole = false;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public static Role createSystemRole(String name, String description) {
        Role role = new Role();
        role.name = name;
        role.description = description;
        role.systemRole = true;
        return role;
    }

    public static Role create(String name, String description) {
        Role role = new Role();
        role.name = name;
        role.description = description;
        return role;
    }

    public void update(String name, String description) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (description != null) this.description = description;
    }

    public void deactivate() {
        this.active = false;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}

package com.education.stelar.identity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String code;

    @Column(length = 300)
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 50)
    private String action;

    @Column(length = 100)
    private String resource;

    @Column(nullable = false, length = 20)
    private String type = "BACKEND";

    @Column(nullable = false)
    private boolean active = true;

    public static Permission create(String code, String description, String category, String action, String resource, String type) {
        Permission p = new Permission();
        p.code = code;
        p.description = description;
        p.category = category;
        p.action = action;
        p.resource = resource;
        p.type = type != null ? type : "BACKEND";
        return p;
    }

    public void update(String description, String category, String action, String resource) {
        if (description != null) this.description = description;
        if (category != null && !category.isBlank()) this.category = category.trim();
        if (action != null) this.action = action.trim();
        if (resource != null) this.resource = resource.trim();
    }

    public void deactivate() {
        this.active = false;
    }
}

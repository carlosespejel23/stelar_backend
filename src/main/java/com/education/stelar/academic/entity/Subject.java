package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "subjects",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subject extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(nullable = false)
    private boolean active = true;

    public static Subject create(String name, String description, UUID teacherId) {
        Subject subject = new Subject();
        subject.name = name.trim();
        subject.description = description != null ? description.trim() : null;
        subject.teacherId = teacherId;
        return subject;
    }

    public void update(String name, String description) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (description != null) this.description = description.trim();
    }

    public void deactivate() {
        this.active = false;
    }
}

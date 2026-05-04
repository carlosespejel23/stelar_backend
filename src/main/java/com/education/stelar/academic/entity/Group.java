package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "groups",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name", "school_year"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String level;

    @Column(name = "school_year", length = 20)
    private String schoolYear;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(name = "academic_period_id")
    private UUID academicPeriodId;

    @Column(nullable = false)
    private boolean active = true;

    public static Group create(String name, String level, String schoolYear, UUID teacherId) {
        Group group = new Group();
        group.name = name.trim();
        group.level = level != null ? level.trim() : null;
        group.schoolYear = schoolYear != null ? schoolYear.trim() : null;
        group.teacherId = teacherId;
        return group;
    }

    public void update(String name, String level) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (level != null) this.level = level.trim();
    }

    public void deactivate() {
        this.active = false;
    }
}

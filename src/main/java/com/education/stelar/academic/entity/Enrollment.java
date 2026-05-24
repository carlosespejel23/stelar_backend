package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_enrollment",
                columnNames = {"tenant_id", "student_id", "group_id", "subject_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "school_year", nullable = false, length = 20)
    private String schoolYear;

    @Column(nullable = false)
    private boolean active = true;

    public static Enrollment create(UUID studentId, UUID groupId, UUID subjectId, String schoolYear) {
        Enrollment enrollment = new Enrollment();
        enrollment.studentId = studentId;
        enrollment.groupId = groupId;
        enrollment.subjectId = subjectId;
        enrollment.schoolYear = schoolYear;
        return enrollment;
    }

    public void deactivate() {
        this.active = false;
    }
}

package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "attendances",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_attendance",
                columnNames = {"enrollment_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "justification_reason", length = 500)
    private String justificationReason;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    public static Attendance record(Enrollment enrollment, LocalDate date,
                                    AttendanceStatus status, UUID recordedBy) {
        Attendance a = new Attendance();
        a.enrollment = enrollment;
        a.date = date;
        a.status = status;
        a.recordedBy = recordedBy;
        return a;
    }

    public void updateStatus(AttendanceStatus newStatus, String reason) {
        this.status = newStatus;
        this.justificationReason = (newStatus == AttendanceStatus.JUSTIFIED) ? reason : null;
    }

    public boolean countsAsAbsent() {
        return this.status == AttendanceStatus.ABSENT;
    }

    public boolean countsAsPresent() {
        return this.status == AttendanceStatus.PRESENT || this.status == AttendanceStatus.LATE;
    }
}

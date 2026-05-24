package com.education.stelar.academic.entity;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "student_code", length = 50)
    private String studentCode;

    @Column(length = 200)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    public static Student create(String firstName, String lastName, String studentCode, String email) {
        Student student = new Student();
        student.firstName = firstName.trim();
        student.lastName = lastName.trim();
        student.studentCode = studentCode != null ? studentCode.trim() : null;
        student.email = email != null ? email.toLowerCase().trim() : null;
        return student;
    }

    public void updateProfile(String firstName, String lastName, String email) {
        if (firstName != null && !firstName.isBlank()) this.firstName = firstName.trim();
        if (lastName != null && !lastName.isBlank()) this.lastName = lastName.trim();
        if (email != null && !email.isBlank()) this.email = email.toLowerCase().trim();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

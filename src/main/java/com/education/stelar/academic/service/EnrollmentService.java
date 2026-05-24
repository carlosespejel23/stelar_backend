package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.EnrollStudentRequest;
import com.education.stelar.academic.dto.response.EnrollmentResponse;
import com.education.stelar.academic.dto.response.GroupStudentResponse;
import com.education.stelar.academic.entity.Enrollment;
import com.education.stelar.academic.entity.Student;
import com.education.stelar.academic.event.StudentEnrolledEvent;
import com.education.stelar.academic.repository.EnrollmentRepository;
import com.education.stelar.academic.repository.GroupRepository;
import com.education.stelar.academic.repository.StudentRepository;
import com.education.stelar.academic.repository.SubjectRepository;
import com.education.stelar.kernel.event.EventPublisher;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final EventPublisher eventPublisher;

    public List<EnrollmentResponse> findByStudent(UUID studentId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        studentRepository.findByIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", studentId));
        return enrollmentRepository.findAllByStudentIdAndTenantId(studentId, tenantId)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional
    public EnrollmentResponse enroll(EnrollStudentRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        studentRepository.findByIdAndTenantId(request.studentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", request.studentId()));

        groupRepository.findByIdAndTenantId(request.groupId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo", request.groupId()));

        subjectRepository.findByIdAndTenantId(request.subjectId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Materia", request.subjectId()));

        if (enrollmentRepository.existsByStudentIdAndGroupIdAndSubjectIdAndTenantId(
                request.studentId(), request.groupId(), request.subjectId(), tenantId)) {
            throw new BusinessException("ENROLLMENT_EXISTS",
                    "El estudiante ya está inscrito en ese grupo y materia.");
        }

        Enrollment enrollment = Enrollment.create(
                request.studentId(), request.groupId(), request.subjectId(), request.schoolYear()
        );
        enrollment = enrollmentRepository.save(enrollment);

        eventPublisher.publish(new StudentEnrolledEvent(
                enrollment.getId(), enrollment.getStudentId(),
                enrollment.getGroupId(), enrollment.getSubjectId(), tenantId));

        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public void unenroll(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", enrollmentId));
        enrollment.deactivate();
        enrollmentRepository.save(enrollment);
    }

    public List<GroupStudentResponse> findStudentsByGroupAndSubject(UUID groupId, UUID subjectId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        return enrollmentRepository.findAllByGroupIdAndSubjectIdAndActiveTrueAndTenantId(groupId, subjectId, tenantId)
                .stream()
                .map(enrollment -> {
                    Student student = studentRepository.findByIdAndTenantId(enrollment.getStudentId(), tenantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Estudiante", enrollment.getStudentId()));
                    return GroupStudentResponse.from(enrollment, student);
                })
                .toList();
    }
}

package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.CreateStudentRequest;
import com.education.stelar.academic.dto.request.UpdateStudentRequest;
import com.education.stelar.academic.dto.response.GradeResponse;
import com.education.stelar.academic.dto.response.StudentDashboardResponse;
import com.education.stelar.academic.dto.response.StudentResponse;
import com.education.stelar.academic.entity.AcademicPeriod;
import com.education.stelar.academic.entity.Attendance;
import com.education.stelar.academic.entity.Enrollment;
import com.education.stelar.academic.entity.Grade;
import com.education.stelar.academic.entity.Group;
import com.education.stelar.academic.entity.Student;
import com.education.stelar.academic.entity.Subject;
import com.education.stelar.academic.repository.AcademicPeriodRepository;
import com.education.stelar.academic.repository.AttendanceRepository;
import com.education.stelar.academic.repository.EnrollmentRepository;
import com.education.stelar.academic.repository.GradeRepository;
import com.education.stelar.academic.repository.GroupRepository;
import com.education.stelar.academic.repository.StudentRepository;
import com.education.stelar.academic.repository.SubjectRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;
import com.education.stelar.kernel.pagination.PagedResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicPeriodRepository academicPeriodRepository;

    public PagedResponse<StudentResponse> findAll(Pageable pageable) {
        return PagedResponse.from(
                studentRepository.findAllByTenantId(TenantContext.getCurrentTenant(), pageable),
                StudentResponse::from
        );
    }

    public StudentResponse findById(UUID id) {
        return studentRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .map(StudentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (request.studentCode() != null && !request.studentCode().isBlank() &&
                studentRepository.existsByStudentCodeAndTenantId(request.studentCode(), tenantId)) {
            throw new BusinessException("STUDENT_CODE_EXISTS",
                    "Ya existe un estudiante con el código: " + request.studentCode());
        }

        Student student = Student.create(
                request.firstName(), request.lastName(), request.studentCode(), request.email()
        );
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(UUID id, UpdateStudentRequest request) {
        Student student = studentRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));

        student.updateProfile(request.firstName(), request.lastName(), request.email());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public void deactivate(UUID id) {
        Student student = studentRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
        student.deactivate();
        studentRepository.save(student);
    }

    public StudentDashboardResponse getDashboard(UUID studentId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Student student = studentRepository.findByIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", studentId));

        // Resolve the active academic period once for weighted average calculations
        UUID activePeriodId = academicPeriodRepository.findFirstByTenantIdAndActiveTrue(tenantId)
                .map(AcademicPeriod::getId)
                .orElse(null);

        List<Enrollment> enrollments = enrollmentRepository.findAllByStudentIdAndTenantId(studentId, tenantId)
                .stream()
                .filter(Enrollment::isActive)
                .toList();

        List<StudentDashboardResponse.EnrollmentSummary> summaries = enrollments.stream()
                .map(enrollment -> buildEnrollmentSummary(enrollment, activePeriodId, tenantId))
                .toList();

        return new StudentDashboardResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getFullName(),
                student.getStudentCode(),
                student.getEmail(),
                summaries
        );
    }

    private StudentDashboardResponse.EnrollmentSummary buildEnrollmentSummary(
            Enrollment enrollment, UUID activePeriodId, UUID tenantId) {

        Group group = groupRepository.findByIdAndTenantId(enrollment.getGroupId(), tenantId).orElse(null);
        Subject subject = subjectRepository.findByIdAndTenantId(enrollment.getSubjectId(), tenantId).orElse(null);

        List<Grade> grades = gradeRepository.findAllByEnrollmentIdAndTenantId(enrollment.getId(), tenantId);

        BigDecimal weightedAverage = null;
        if (activePeriodId != null) {
            weightedAverage = gradeRepository.findWeightedAverageByStudentAndSubjectAndPeriod(
                            enrollment.getStudentId(), enrollment.getSubjectId(), activePeriodId, tenantId)
                    .map(avg -> avg.setScale(2, RoundingMode.HALF_UP))
                    .orElse(BigDecimal.ZERO);
        }

        List<Attendance> attendances = attendanceRepository.findAllByEnrollmentId(enrollment.getId());
        StudentDashboardResponse.AttendanceSummary attendanceSummary = buildAttendanceSummary(attendances);

        return new StudentDashboardResponse.EnrollmentSummary(
                enrollment.getId(),
                enrollment.getGroupId(),
                group != null ? group.getName() : null,
                enrollment.getSubjectId(),
                subject != null ? subject.getName() : null,
                weightedAverage,
                attendanceSummary,
                grades.stream().map(GradeResponse::from).toList()
        );
    }

    private StudentDashboardResponse.AttendanceSummary buildAttendanceSummary(List<Attendance> attendances) {
        int present = 0, absent = 0, late = 0, justified = 0;
        for (Attendance a : attendances) {
            switch (a.getStatus()) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case LATE -> late++;
                case JUSTIFIED -> justified++;
            }
        }
        int total = attendances.size();
        int attendancePct = total == 0 ? 100 : (int) Math.round(((double)(present + late) / total) * 100);

        return new StudentDashboardResponse.AttendanceSummary(total, present, absent, late, justified, attendancePct);
    }
}

package com.education.stelar.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.academic.dto.request.CreateStudentRequest;
import com.education.stelar.academic.dto.request.EnrollStudentRequest;
import com.education.stelar.academic.dto.request.UpdateStudentRequest;
import com.education.stelar.academic.dto.response.EnrollmentResponse;
import com.education.stelar.academic.dto.response.StudentDashboardResponse;
import com.education.stelar.academic.dto.response.StudentResponse;
import com.education.stelar.academic.service.EnrollmentService;
import com.education.stelar.academic.service.StudentService;
import com.education.stelar.kernel.pagination.PagedResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Gestión de estudiantes e inscripciones")
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @GetMapping
    @Operation(summary = "Listar estudiantes del tenant (paginado)")
    public ResponseEntity<PagedResponse<StudentResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                studentService.findAll(PageRequest.of(page, size, Sort.by("lastName", "firstName")))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil completo del estudiante")
    public ResponseEntity<StudentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo estudiante")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos del estudiante")
    public ResponseEntity<StudentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar estudiante")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        studentService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Dashboard académico completo del estudiante: inscripciones, calificaciones, asistencia y promedios ponderados")
    public ResponseEntity<StudentDashboardResponse> getDashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.getDashboard(id));
    }

    // ── Enrollments sub-resource ─────────────────────────────────────

    @GetMapping("/{id}/enrollments")
    @Operation(summary = "Listar inscripciones de un estudiante")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollments(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.findByStudent(id));
    }

    @PostMapping("/enrollments")
    @Operation(summary = "Inscribir estudiante en grupo y materia")
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(request));
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    @Operation(summary = "Cancelar inscripción")
    public ResponseEntity<Void> unenroll(@PathVariable UUID enrollmentId) {
        enrollmentService.unenroll(enrollmentId);
        return ResponseEntity.noContent().build();
    }
}

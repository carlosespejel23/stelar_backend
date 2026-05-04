package com.education.stelar.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.academic.dto.request.BatchGradeRequest;
import com.education.stelar.academic.dto.request.RecordGradeRequest;
import com.education.stelar.academic.dto.response.GradeResponse;
import com.education.stelar.academic.service.GradeService;
import com.education.stelar.kernel.security.UserPrincipal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Registro y consulta de calificaciones")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @Operation(summary = "Registrar calificación (crea o actualiza para la inscripción y período)")
    public ResponseEntity<GradeResponse> record(
            @Valid @RequestBody RecordGradeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.record(request, principal.getId()));
    }

    @GetMapping("/student/{id}")
    @Operation(summary = "Todas las calificaciones de un estudiante")
    public ResponseEntity<List<GradeResponse>> byStudent(@PathVariable UUID id) {
        return ResponseEntity.ok(gradeService.findByStudent(id));
    }

    @GetMapping("/subject/{id}")
    @Operation(summary = "Calificaciones de una materia")
    public ResponseEntity<List<GradeResponse>> bySubject(@PathVariable UUID id) {
        return ResponseEntity.ok(gradeService.findBySubject(id));
    }

    @GetMapping("/student/{studentId}/subject/{subjectId}")
    @Operation(summary = "Calificaciones de un estudiante en una materia específica")
    public ResponseEntity<List<GradeResponse>> byStudentAndSubject(
            @PathVariable UUID studentId,
            @PathVariable UUID subjectId) {
        return ResponseEntity.ok(gradeService.findByStudentAndSubject(studentId, subjectId));
    }

    @GetMapping("/group/{groupId}/subject/{subjectId}")
    @Operation(summary = "Calificaciones de todos los estudiantes de un grupo en una materia")
    public ResponseEntity<List<GradeResponse>> byGroupAndSubject(
            @PathVariable UUID groupId,
            @PathVariable UUID subjectId) {
        return ResponseEntity.ok(gradeService.findByGroupAndSubject(groupId, subjectId));
    }

    @PostMapping("/batch")
    @Operation(summary = "Registrar calificaciones en lote para un grupo, materia y período de evaluación")
    public ResponseEntity<List<GradeResponse>> recordBatch(
            @Valid @RequestBody BatchGradeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.recordBatch(request, principal.getId()));
    }

    @GetMapping("/student/{studentId}/subject/{subjectId}/average")
    @Operation(summary = "Promedio ponderado de un estudiante en una materia (usa el ciclo activo si no se especifica)")
    public ResponseEntity<Map<String, BigDecimal>> average(
            @PathVariable UUID studentId,
            @PathVariable UUID subjectId,
            @RequestParam(required = false) UUID academicPeriodId) {
        BigDecimal avg = gradeService.getWeightedAverage(studentId, subjectId, academicPeriodId);
        return ResponseEntity.ok(Map.of("weightedAverage", avg));
    }
}

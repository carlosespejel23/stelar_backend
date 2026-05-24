package com.education.stelar.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.academic.dto.request.BatchAttendanceRequest;
import com.education.stelar.academic.dto.request.RecordAttendanceRequest;
import com.education.stelar.academic.dto.request.UpdateAttendanceRequest;
import com.education.stelar.academic.dto.response.AttendanceResponse;
import com.education.stelar.academic.service.AttendanceService;
import com.education.stelar.kernel.security.UserPrincipal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Asistencia", description = "Registro y consulta de asistencia")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Registrar asistencia individual (crea o actualiza el registro del día)")
    public ResponseEntity<AttendanceResponse> record(
            @Valid @RequestBody RecordAttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.record(request, principal.getId()));
    }

    @PostMapping("/batch")
    @Operation(summary = "Pase de lista completo para un grupo, materia y fecha")
    public ResponseEntity<List<AttendanceResponse>> recordBatch(
            @Valid @RequestBody BatchAttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.recordBatch(request, principal.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar o justificar un registro de asistencia")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.update(id, request));
    }

    @GetMapping("/student/{id}")
    @Operation(summary = "Historial de asistencia de un estudiante")
    public ResponseEntity<List<AttendanceResponse>> byStudent(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.findByStudent(id));
    }

    @GetMapping("/subject/{id}")
    @Operation(summary = "Registros de asistencia de una materia")
    public ResponseEntity<List<AttendanceResponse>> bySubject(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.findBySubject(id));
    }

    @GetMapping("/group/{id}")
    @Operation(summary = "Registros de asistencia de un grupo")
    public ResponseEntity<List<AttendanceResponse>> byGroup(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.findByGroup(id));
    }

    @GetMapping("/group/{groupId}/date/{date}")
    @Operation(summary = "Pase de lista de un grupo en una fecha específica")
    public ResponseEntity<List<AttendanceResponse>> byGroupAndDate(
            @PathVariable UUID groupId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.findByGroupAndDate(groupId, date));
    }
}

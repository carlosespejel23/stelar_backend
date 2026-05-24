package com.education.stelar.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.academic.dto.request.CreateAcademicPeriodRequest;
import com.education.stelar.academic.dto.request.UpdateAcademicPeriodRequest;
import com.education.stelar.academic.dto.response.AcademicPeriodResponse;
import com.education.stelar.academic.service.AcademicPeriodService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic/periods")
@RequiredArgsConstructor
@Tag(name = "Ciclos académicos", description = "Gestión de ciclos y períodos de evaluación")
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    @PostMapping
    @Operation(summary = "Crear ciclo académico con sus períodos de evaluación")
    public ResponseEntity<AcademicPeriodResponse> create(@Valid @RequestBody CreateAcademicPeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPeriodService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos los ciclos académicos del tenant")
    public ResponseEntity<List<AcademicPeriodResponse>> findAll() {
        return ResponseEntity.ok(academicPeriodService.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Obtener el ciclo académico activo")
    public ResponseEntity<AcademicPeriodResponse> findActive() {
        return ResponseEntity.ok(academicPeriodService.findActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ciclo académico por ID con todos sus períodos de evaluación")
    public ResponseEntity<AcademicPeriodResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(academicPeriodService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar fechas y nombre del ciclo académico")
    public ResponseEntity<AcademicPeriodResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAcademicPeriodRequest request) {
        return ResponseEntity.ok(academicPeriodService.update(id, request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activar ciclo académico (desactiva los demás)")
    public ResponseEntity<AcademicPeriodResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(academicPeriodService.activate(id));
    }
}

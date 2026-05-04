package com.education.stelar.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.academic.dto.request.CreateSubjectRequest;
import com.education.stelar.academic.dto.request.UpdateSubjectRequest;
import com.education.stelar.academic.dto.response.SubjectResponse;
import com.education.stelar.academic.service.SubjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@Tag(name = "Materias", description = "Gestión de materias")
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    @Operation(summary = "Listar todas las materias del tenant")
    public ResponseEntity<List<SubjectResponse>> findAll() {
        return ResponseEntity.ok(subjectService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener materia por ID")
    public ResponseEntity<SubjectResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(subjectService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear nueva materia")
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar materia")
    public ResponseEntity<SubjectResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        return ResponseEntity.ok(subjectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar materia")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        subjectService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

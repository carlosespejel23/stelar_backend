package com.education.stelar.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.academic.dto.request.CreateGroupRequest;
import com.education.stelar.academic.dto.request.UpdateGroupRequest;
import com.education.stelar.academic.dto.response.GroupResponse;
import com.education.stelar.academic.dto.response.GroupStudentResponse;
import com.education.stelar.academic.service.EnrollmentService;
import com.education.stelar.academic.service.GroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Grupos", description = "Gestión de grupos escolares")
public class GroupController {

    private final GroupService groupService;
    private final EnrollmentService enrollmentService;

    @GetMapping
    @Operation(summary = "Listar todos los grupos del tenant")
    public ResponseEntity<List<GroupResponse>> findAll() {
        return ResponseEntity.ok(groupService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener grupo por ID")
    public ResponseEntity<GroupResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo grupo")
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar grupo")
    public ResponseEntity<GroupResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGroupRequest request) {
        return ResponseEntity.ok(groupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar grupo")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        groupService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/subjects/{subjectId}/students")
    @Operation(summary = "Listar estudiantes inscritos en un grupo y materia específica")
    public ResponseEntity<List<GroupStudentResponse>> findStudents(
            @PathVariable UUID groupId,
            @PathVariable UUID subjectId) {
        return ResponseEntity.ok(enrollmentService.findStudentsByGroupAndSubject(groupId, subjectId));
    }
}

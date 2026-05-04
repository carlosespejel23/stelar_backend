package com.education.stelar.identity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.identity.dto.request.CreateUserRequest;
import com.education.stelar.identity.dto.request.UpdateUserRequest;
import com.education.stelar.identity.dto.response.UserResponse;
import com.education.stelar.identity.service.UserDetailsImpl;
import com.education.stelar.identity.service.UserService;
import com.education.stelar.kernel.pagination.PagedResponse;
import com.education.stelar.kernel.security.CurrentUser;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios dentro del tenant")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Listar usuarios del tenant (paginado)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                userService.findAll(PageRequest.of(page, size, Sort.by("lastName", "firstName")))
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener datos del usuario autenticado")
    public ResponseEntity<UserResponse> me(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.ok(userService.findById(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear usuario dentro del tenant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario (soft delete)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

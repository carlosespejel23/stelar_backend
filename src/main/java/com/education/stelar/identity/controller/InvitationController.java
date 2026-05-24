package com.education.stelar.identity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.identity.dto.request.*;
import com.education.stelar.identity.dto.response.InvitationResponse;
import com.education.stelar.identity.dto.response.UserResponse;
import com.education.stelar.identity.service.InvitationService;
import com.education.stelar.identity.service.UserDetailsImpl;
import com.education.stelar.kernel.security.CurrentUser;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitaciones", description = "Gestión de invitaciones para unirse al tenant")
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping
    @Operation(summary = "Listar invitaciones del tenant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationResponse>> findAll() {
        return ResponseEntity.ok(invitationService.findAll());
    }

    @PostMapping("/external")
    @Operation(summary = "Invitar a un usuario externo (se envía email con enlace de registro)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationResponse> inviteExternal(
            @Valid @RequestBody InviteExternalRequest request,
            @CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.inviteExternal(request, currentUser.getId()));
    }

    @PostMapping("/internal")
    @Operation(summary = "Invitar a un usuario interno (ya existe en el sistema, sin email)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationResponse> inviteInternal(
            @Valid @RequestBody InviteInternalRequest request,
            @CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.inviteInternal(request, currentUser.getId()));
    }

    @PostMapping("/accept")
    @Operation(summary = "Aceptar invitación interna con token (usuario ya existe en el sistema)")
    public ResponseEntity<UserResponse> accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return ResponseEntity.ok(invitationService.accept(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrarse a partir de una invitación externa (usuario nuevo)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterFromInvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.registerFromInvitation(request));
    }

    @PostMapping("/revoke")
    @Operation(summary = "Revocar invitación pendiente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revoke(@Valid @RequestBody RevokeInvitationRequest request) {
        invitationService.revoke(request);
        return ResponseEntity.noContent().build();
    }
}

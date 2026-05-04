package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.dto.request.CreatePermissionRequest;
import com.education.stelar.identity.dto.request.UpdatePermissionRequest;
import com.education.stelar.identity.dto.response.PermissionResponse;
import com.education.stelar.identity.entity.Permission;
import com.education.stelar.identity.repository.PermissionRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public List<PermissionResponse> findAll() {
        return permissionRepository.findAllByActive(true)
                .stream()
                .map(PermissionResponse::from)
                .toList();
    }

    public PermissionResponse findById(UUID id) {
        return permissionRepository.findById(id)
                .map(PermissionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso", id));
    }

    @Transactional
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("PERMISSION_CODE_EXISTS",
                    "Ya existe un permiso con el código: " + request.code());
        }
        Permission permission = Permission.create(
                request.code(), request.description(),
                request.category(), request.action(),
                request.resource(), request.type()
        );
        return PermissionResponse.from(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionResponse update(UUID id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso", id));

        permission.update(request.description(), request.category(), request.action(), request.resource());
        return PermissionResponse.from(permissionRepository.save(permission));
    }
}

package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.dto.request.CreateRoleRequest;
import com.education.stelar.identity.dto.response.RoleResponse;
import com.education.stelar.identity.entity.Permission;
import com.education.stelar.identity.entity.Role;
import com.education.stelar.identity.repository.PermissionRepository;
import com.education.stelar.identity.repository.RoleRepository;
import com.education.stelar.identity.repository.UserTenantRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserTenantRepository userTenantRepository;

    public List<RoleResponse> findAll() {
        return roleRepository.findAllByTenantIdAndActive(TenantContext.getCurrentTenant(), true)
                .stream()
                .map(RoleResponse::from)
                .toList();
    }

    public RoleResponse findById(UUID id) {
        return roleRepository.findById(id)
                .filter(r -> r.getTenantId().equals(TenantContext.getCurrentTenant()))
                .map(RoleResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));
    }

    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (roleRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new BusinessException("ROLE_NAME_EXISTS", "Ya existe un rol con el nombre: " + request.name());
        }
        Role role = Role.create(request.name(), request.description());
        assignPermissions(role, request.permissionIds());
        return RoleResponse.from(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse update(UUID id, CreateRoleRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Role role = roleRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));

        if (role.isSystemRole()) {
            throw new BusinessException("SYSTEM_ROLE_IMMUTABLE", "Los roles del sistema no pueden modificarse.");
        }

        // Validar unicidad del nuevo nombre si cambió
        if (!role.getName().equalsIgnoreCase(request.name())
                && roleRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new BusinessException("ROLE_NAME_EXISTS", "Ya existe un rol con el nombre: " + request.name());
        }

        role.update(request.name(), request.description());
        assignPermissions(role, request.permissionIds());
        return RoleResponse.from(roleRepository.save(role));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Role role = roleRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));

        if (role.isSystemRole()) {
            throw new BusinessException("SYSTEM_ROLE_IMMUTABLE", "Los roles del sistema no pueden eliminarse.");
        }

        if (userTenantRepository.existsByRole_Id(id)) {
            throw new BusinessException("ROLE_IN_USE",
                    "No se puede eliminar un rol que tiene usuarios asignados. Reasígnalos primero.");
        }

        roleRepository.delete(role);
        log.info("Rol custom eliminado: {} (tenant: {})", role.getName(), tenantId);
    }

    // ── Helper ────────────────────────────────────────────────────────

    private void assignPermissions(Role role, List<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            role.setPermissions(new HashSet<>());
            return;
        }
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.setPermissions(new HashSet<>(permissions));
    }
}

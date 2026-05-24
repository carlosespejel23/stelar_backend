package com.education.stelar.identity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.identity.entity.UserTenant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, UUID> {

    /**
     * Carga UserTenant + User + Role + permisos del rol en un solo JOIN.
     * Evita los 3 queries de lazy loading que ocurren en el flujo de auth.
     */
    @EntityGraph(attributePaths = {"user", "role", "role.permissions"})
    Optional<UserTenant> findByUser_EmailAndTenantId(String email, UUID tenantId);

    /**
     * Igual que el anterior pero lookup por userId en lugar de email.
     * Usado en buildAuthResponse, getMyPermissions, getSessionInfo.
     */
    @EntityGraph(attributePaths = {"user", "role", "role.permissions"})
    Optional<UserTenant> findByUser_IdAndTenantId(UUID userId, UUID tenantId);

    List<UserTenant> findAllByTenantId(UUID tenantId);

    Page<UserTenant> findAllByTenantId(UUID tenantId, Pageable pageable);

    List<UserTenant> findAllByTenantIdAndActive(UUID tenantId, boolean active);

    boolean existsByUser_EmailAndTenantId(String email, UUID tenantId);

    boolean existsByUser_IdAndTenantId(UUID userId, UUID tenantId);

    List<UserTenant> findAllByUser_Email(String email);

    List<UserTenant> findAllByUser_Id(UUID userId);

    boolean existsByRole_Id(UUID roleId);
}

package com.education.stelar.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.identity.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndTenantId(String name, UUID tenantId);

    List<Role> findAllByTenantId(UUID tenantId);

    List<Role> findAllByTenantIdAndActive(UUID tenantId, boolean active);

    boolean existsByNameAndTenantId(String name, UUID tenantId);
}

package com.education.stelar.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.identity.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    List<Permission> findAllByActive(boolean active);

    List<Permission> findAllByCategory(String category);
}

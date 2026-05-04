package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.Group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    List<Group> findAllByTenantId(UUID tenantId);

    List<Group> findAllByTenantIdAndActive(UUID tenantId, boolean active);

    List<Group> findAllByTenantIdAndTeacherId(UUID tenantId, UUID teacherId);

    Optional<Group> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndSchoolYearAndTenantId(String name, String schoolYear, UUID tenantId);
}

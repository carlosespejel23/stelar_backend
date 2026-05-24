package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.CreateGroupRequest;
import com.education.stelar.academic.dto.request.UpdateGroupRequest;
import com.education.stelar.academic.dto.response.GroupResponse;
import com.education.stelar.academic.entity.Group;
import com.education.stelar.academic.repository.GroupRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;

    public List<GroupResponse> findAll() {
        return groupRepository.findAllByTenantId(TenantContext.getCurrentTenant())
                .stream()
                .map(GroupResponse::from)
                .toList();
    }

    public GroupResponse findById(UUID id) {
        return groupRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .map(GroupResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo", id));
    }

    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (request.schoolYear() != null &&
                groupRepository.existsByNameAndSchoolYearAndTenantId(request.name(), request.schoolYear(), tenantId)) {
            throw new BusinessException("GROUP_ALREADY_EXISTS",
                    "Ya existe un grupo con el nombre '" + request.name() + "' en el ciclo " + request.schoolYear());
        }

        Group group = Group.create(request.name(), request.level(), request.schoolYear(), request.teacherId());
        return GroupResponse.from(groupRepository.save(group));
    }

    @Transactional
    public GroupResponse update(UUID id, UpdateGroupRequest request) {
        Group group = groupRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo", id));

        group.update(request.name(), request.level());
        return GroupResponse.from(groupRepository.save(group));
    }

    @Transactional
    public void deactivate(UUID id) {
        Group group = groupRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo", id));
        group.deactivate();
        groupRepository.save(group);
    }
}

package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.CreateSubjectRequest;
import com.education.stelar.academic.dto.request.UpdateSubjectRequest;
import com.education.stelar.academic.dto.response.SubjectResponse;
import com.education.stelar.academic.entity.Subject;
import com.education.stelar.academic.repository.SubjectRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public List<SubjectResponse> findAll() {
        return subjectRepository.findAllByTenantId(TenantContext.getCurrentTenant())
                .stream()
                .map(SubjectResponse::from)
                .toList();
    }

    public SubjectResponse findById(UUID id) {
        return subjectRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .map(SubjectResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Materia", id));
    }

    @Transactional
    public SubjectResponse create(CreateSubjectRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (subjectRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new BusinessException("SUBJECT_ALREADY_EXISTS",
                    "Ya existe una materia con el nombre: " + request.name());
        }

        Subject subject = Subject.create(request.name(), request.description(), request.teacherId());
        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(UUID id, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Materia", id));

        subject.update(request.name(), request.description());
        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Transactional
    public void deactivate(UUID id) {
        Subject subject = subjectRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Materia", id));
        subject.deactivate();
        subjectRepository.save(subject);
    }
}

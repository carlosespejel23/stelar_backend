package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.dto.request.CreateUserRequest;
import com.education.stelar.identity.dto.request.UpdateUserRequest;
import com.education.stelar.identity.dto.response.UserResponse;
import com.education.stelar.identity.entity.Role;
import com.education.stelar.identity.entity.User;
import com.education.stelar.identity.entity.UserTenant;
import com.education.stelar.identity.repository.RoleRepository;
import com.education.stelar.identity.repository.UserRepository;
import com.education.stelar.identity.repository.UserTenantRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;
import com.education.stelar.kernel.pagination.PagedResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PagedResponse<UserResponse> findAll(Pageable pageable) {
        return PagedResponse.from(
                userTenantRepository.findAllByTenantId(TenantContext.getCurrentTenant(), pageable),
                ut -> UserResponse.from(ut.getUser(), ut)
        );
    }

    public UserResponse findById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UserTenant userTenant = userTenantRepository.findByUser_IdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return UserResponse.from(userTenant.getUser(), userTenant);
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (userTenantRepository.existsByUser_EmailAndTenantId(request.email(), tenantId)) {
            throw new BusinessException("USER_EMAIL_EXISTS",
                    "Ya existe un usuario con el email: " + request.email());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("USER_EMAIL_EXISTS",
                    "Ya existe un usuario con el email: " + request.email() +
                            ". Usa invitaciones para agregar a un usuario existente.");
        }

        Role role = roleRepository.findById(request.roleId())
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Rol", request.roleId()));

        User user = userRepository.save(User.create(
                request.firstName(), request.lastName(),
                request.email(), passwordEncoder.encode(request.password()),
                request.profession()
        ));

        UserTenant userTenant = userTenantRepository.save(UserTenant.create(user, role));
        return UserResponse.from(user, userTenant);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UserTenant userTenant = userTenantRepository.findByUser_IdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        User user = userTenant.getUser();
        user.updateProfile(request.firstName(), request.lastName(), request.profilePictureUrl(), request.profession());
        userRepository.save(user);

        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .filter(r -> r.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", request.roleId()));
            userTenant.assignRole(role);
        }

        return UserResponse.from(user, userTenantRepository.save(userTenant));
    }

    @Transactional
    public void deactivate(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        UserTenant userTenant = userTenantRepository.findByUser_IdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        userTenant.deactivate();
        userTenantRepository.save(userTenant);
    }
}

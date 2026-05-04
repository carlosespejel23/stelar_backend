package com.academic.stellar.identity.application;

import com.academic.stellar.shared.IntegrationTestBase;
import com.education.stelar.identity.dto.request.*;
import com.education.stelar.identity.dto.response.UserResponse;
import com.education.stelar.identity.entity.Profession;
import com.education.stelar.identity.repository.RoleRepository;
import com.education.stelar.identity.repository.TenantRepository;
import com.education.stelar.identity.repository.UserRepository;
import com.education.stelar.identity.repository.UserTenantRepository;
import com.education.stelar.identity.service.AuthService;
import com.education.stelar.identity.service.RoleService;
import com.education.stelar.identity.service.UserService;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserService — tests de integración")
class UserServiceIntegrationTest extends IntegrationTestBase {

    @Autowired AuthService authService;
    @Autowired UserService userService;
    @Autowired RoleService roleService;
    @Autowired TenantRepository tenantRepository;
    @Autowired UserRepository userRepository;
    @Autowired UserTenantRepository userTenantRepository;
    @Autowired RoleRepository roleRepository;

    private static final String ADMIN_EMAIL = "admin@userservice.mx";
    private static final String PASS        = "Password123!";

    /** Slug generado por el backend al registrar — se captura en setup. */
    private String slug;
    private UUID tenantId;
    private UUID teacherRoleId;

    @BeforeEach
    @Transactional
    void setup() {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            slug = authService.register(new RegisterRequest(
                    "Colegio UserService", "Luis", "Pérez", ADMIN_EMAIL, PASS, null, null
            ));
        } else {
            // El tenant ya existe — recuperar el slug desde la BD
            var ut = userTenantRepository.findAllByUser_Email(ADMIN_EMAIL).stream()
                    .findFirst().orElseThrow();
            slug = tenantRepository.findById(ut.getTenantId())
                    .map(t -> t.getSlug())
                    .orElseThrow();
        }

        var tenant = tenantRepository.findBySlug(slug).orElseThrow();
        tenantId = tenant.getId();
        TenantContext.setCurrentTenant(tenantId);

        userRepository.findByEmail(ADMIN_EMAIL).ifPresent(u -> {
            if (!u.isEmailVerified()) {
                u.verifyEmail();
                userRepository.save(u);
            }
        });
        userTenantRepository.findByUser_EmailAndTenantId(ADMIN_EMAIL, tenantId).ifPresent(ut -> {
            if (!ut.isActive()) {
                ut.activate();
                userTenantRepository.save(ut);
            }
        });

        teacherRoleId = roleRepository.findByNameAndTenantId("TEACHER", tenantId)
                .map(r -> r.getId())
                .orElseThrow();
    }

    @Test
    @DisplayName("should_create_user_when_valid_request")
    void should_create_user_when_valid_request() {
        var request = new CreateUserRequest(
                "Elena", "Torres", "elena@colegio.mx", "Password123!", teacherRoleId, Profession.DRA
        );

        UserResponse response = userService.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("elena@colegio.mx");
        assertThat(response.tenantId()).isEqualTo(tenantId);
        assertThat(response.roleName()).isEqualTo("TEACHER");
        assertThat(response.active()).isFalse();
        assertThat(response.emailVerified()).isFalse();
    }

    @Test
    @DisplayName("should_fail_create_when_email_already_exists_in_tenant")
    void should_fail_create_when_email_already_exists_in_tenant() {
        var request = new CreateUserRequest(
                "Duplicado", "Test", ADMIN_EMAIL, "Password123!", teacherRoleId, Profession.PROFA
        );

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("should_fail_create_when_role_does_not_belong_to_tenant")
    void should_fail_create_when_role_does_not_belong_to_tenant() {
        UUID fakeRoleId = UUID.randomUUID();
        var request = new CreateUserRequest(
                "X", "Y", "xy@mail.mx", "Password123!", fakeRoleId, Profession.ING
        );

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_return_all_users_in_current_tenant")
    void should_return_all_users_in_current_tenant() {
        var users = userService.findAll(PageRequest.of(0, 50));

        assertThat(users.content()).isNotEmpty();
        assertThat(users.content()).allMatch(u -> u.tenantId().equals(tenantId));
    }

    @Test
    @Transactional
    @DisplayName("should_update_user_profile_when_valid_request")
    void should_update_user_profile_when_valid_request() {
        UUID userId = userService.create(new CreateUserRequest(
                "Original", "Nombre", "original@mail.mx", "Password123!", teacherRoleId, Profession.LIC
        )).id();

        var updateReq = new UpdateUserRequest("Actualizado", "Apellido", null, null, null);
        UserResponse updated = userService.update(userId, updateReq);

        assertThat(updated.firstName()).isEqualTo("Actualizado");
        assertThat(updated.lastName()).isEqualTo("Apellido");
    }

    @Test
    @Transactional
    @DisplayName("should_deactivate_user_when_called")
    void should_deactivate_user_when_called() {
        UUID userId = userService.create(new CreateUserRequest(
                "Para", "Desactivar", "desactivar@mail.mx", "Password123!", teacherRoleId, Profession.MTRO
        )).id();

        userService.deactivate(userId);

        var userTenant = userTenantRepository.findByUser_IdAndTenantId(userId, tenantId).orElseThrow();
        assertThat(userTenant.isActive()).isFalse();
    }

    @Test
    @DisplayName("should_fail_find_user_from_different_tenant")
    void should_fail_find_user_from_different_tenant() {
        UUID foreignId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.findById(foreignId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

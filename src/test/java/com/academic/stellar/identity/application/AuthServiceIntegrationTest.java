package com.academic.stellar.identity.application;

import com.academic.stellar.shared.IntegrationTestBase;
import com.education.stelar.identity.dto.request.*;
import com.education.stelar.identity.dto.response.AuthResponse;
import com.education.stelar.identity.dto.response.ValidateResponse;
import com.education.stelar.identity.repository.TenantRepository;
import com.education.stelar.identity.repository.UserRepository;
import com.education.stelar.identity.repository.UserTenantRepository;
import com.education.stelar.identity.service.AuthService;
import com.education.stelar.kernel.exception.BusinessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthService — tests de integración")
class AuthServiceIntegrationTest extends IntegrationTestBase {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired UserTenantRepository userTenantRepository;
    @Autowired TenantRepository tenantRepository;

    private static final String SCHOOL_NAME = "Colegio Integración";
    private static final String ADMIN_EMAIL = "admin@integracion.mx";
    private static final String ADMIN_PASS  = "Password123!";
    private static final String FIRST_NAME  = "Ana";
    private static final String LAST_NAME   = "López";

    /** Slug generado por el backend al registrar — se captura en setup. */
    private String schoolSlug;

    @BeforeEach
    @Transactional
    void setup() {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            // Primer setup: registrar y activar al admin
            schoolSlug = authService.register(new RegisterRequest(
                    SCHOOL_NAME, FIRST_NAME, LAST_NAME, ADMIN_EMAIL, ADMIN_PASS, null, null
            ));
            activateUser(ADMIN_EMAIL);
        } else {
            // Setup subsiguientes: el tenant ya existe, recuperar el slug desde la BD
            var ut = userTenantRepository.findAllByUser_Email(ADMIN_EMAIL).stream()
                    .findFirst().orElseThrow();
            schoolSlug = tenantRepository.findById(ut.getTenantId())
                    .map(t -> t.getSlug())
                    .orElseThrow();
        }
    }

    @Test
    @Transactional
    @DisplayName("should_register_tenant_and_admin_when_valid_request")
    void should_register_tenant_and_admin_when_valid_request() {
        String email = "admin@nuevo.mx";

        String generatedSlug = authService.register(new RegisterRequest(
                "Colegio Nuevo", "Carlos", "Ruiz", email, "Password123!", null, null
        ));

        // El slug es generado por el backend con sufijo aleatorio
        assertThat(generatedSlug).isNotBlank();
        assertThat(generatedSlug).startsWith("colegio-nuevo-");

        var tenant = tenantRepository.findBySlug(generatedSlug);
        assertThat(tenant).isPresent();
        assertThat(tenant.get().getName()).isEqualTo("Colegio Nuevo");

        var user = userRepository.findByEmail(email);
        assertThat(user).isPresent();
        assertThat(user.get().isEmailVerified()).isFalse();

        var userTenant = userTenantRepository.findByUser_EmailAndTenantId(email, tenant.get().getId());
        assertThat(userTenant).isPresent();
        assertThat(userTenant.get().isActive()).isFalse();
        assertThat(userTenant.get().getRoleName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("should_fail_register_when_email_already_exists")
    void should_fail_register_when_email_already_exists() {
        // ADMIN_EMAIL ya está registrado en setup — un segundo registro con el mismo email debe fallar
        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "Otra Escuela", "X", "Y", ADMIN_EMAIL, "Password123!", null, null
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("should_return_tenants_when_valid_credentials")
    void should_return_tenants_when_valid_credentials() {
        ValidateResponse response = authService.validate(new ValidateRequest(ADMIN_EMAIL, ADMIN_PASS));

        assertThat(response.email()).isEqualTo(ADMIN_EMAIL);
        assertThat(response.tenants()).isNotEmpty();
        assertThat(response.tenants().get(0).slug()).isEqualTo(schoolSlug);
    }

    @Test
    @DisplayName("should_fail_validate_when_wrong_password")
    void should_fail_validate_when_wrong_password() {
        assertThatThrownBy(() -> authService.validate(new ValidateRequest(ADMIN_EMAIL, "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("incorrectos");
    }

    @Test
    @DisplayName("should_fail_validate_when_email_not_registered")
    void should_fail_validate_when_email_not_registered() {
        assertThatThrownBy(() -> authService.validate(new ValidateRequest("noexiste@mail.mx", ADMIN_PASS)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("incorrectos");
    }

    @Test
    @DisplayName("should_login_and_return_tokens_when_valid_credentials")
    void should_login_and_return_tokens_when_valid_credentials() {
        AuthResponse response = authService.login(new LoginRequest(ADMIN_EMAIL, ADMIN_PASS, schoolSlug, null));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo(ADMIN_EMAIL);
        assertThat(response.user().roleName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("should_fail_login_when_email_not_verified")
    void should_fail_login_when_email_not_verified() {
        String generatedSlug = authService.register(new RegisterRequest(
                "Sin Verificar", "X", "Y", "sinverificar@mail.mx", "Password123!", null, null
        ));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("sinverificar@mail.mx", "Password123!", generatedSlug, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("verificar");
    }

    @Test
    @DisplayName("should_fail_login_when_tenant_not_found")
    void should_fail_login_when_tenant_not_found() {
        assertThatThrownBy(() -> authService.login(
                new LoginRequest(ADMIN_EMAIL, ADMIN_PASS, "tenant-inexistente", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    @DisplayName("should_refresh_and_return_new_tokens")
    void should_refresh_and_return_new_tokens() {
        AuthResponse loginResponse = authService.login(new LoginRequest(ADMIN_EMAIL, ADMIN_PASS, schoolSlug, null));
        String refreshToken = loginResponse.refreshToken();

        AuthResponse refreshResponse = authService.refresh(new RefreshTokenRequest(refreshToken));

        assertThat(refreshResponse.accessToken()).isNotBlank();
        assertThat(refreshResponse.refreshToken()).isNotBlank();
        assertThat(refreshResponse.refreshToken()).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("should_fail_refresh_when_token_invalid")
    void should_fail_refresh_when_token_invalid() {
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("token-falso-123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("should_return_my_tenants_for_authenticated_user")
    void should_return_my_tenants_for_authenticated_user() {
        var tenants = authService.getMyTenants(ADMIN_EMAIL);

        assertThat(tenants).isNotEmpty();
        assertThat(tenants).anyMatch(t -> t.slug().equals(schoolSlug));
    }

    @Test
    @DisplayName("should_fail_switch_tenant_when_no_account_in_target")
    void should_fail_switch_tenant_when_no_account_in_target() {
        assertThatThrownBy(() -> authService.switchTenant(
                new SwitchTenantRequest("escuela-inexistente"), ADMIN_EMAIL))
                .isInstanceOf(BusinessException.class);
    }

    private void activateUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.verifyEmail();
            userRepository.save(user);
        });
        var tenant = tenantRepository.findBySlug(schoolSlug).orElseThrow();
        userTenantRepository.findByUser_EmailAndTenantId(email, tenant.getId()).ifPresent(ut -> {
            ut.activate();
            userTenantRepository.save(ut);
        });
    }
}

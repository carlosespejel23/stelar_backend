package com.academic.stellar.identity.api;

import com.academic.stellar.shared.IntegrationTestBase;
import com.education.stelar.identity.dto.request.*;
import com.education.stelar.identity.repository.TenantRepository;
import com.education.stelar.identity.repository.UserRepository;
import com.education.stelar.identity.repository.UserTenantRepository;
import com.education.stelar.identity.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("AuthController — tests de integración HTTP")
class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService authService;
    @Autowired TenantRepository tenantRepository;
    @Autowired UserRepository userRepository;
    @Autowired UserTenantRepository userTenantRepository;

    private static final String EMAIL = "admin@http-test.mx";
    private static final String PASS  = "Password123!";

    /** Slug generado por el backend al registrar — se captura en setup. */
    private String slug;

    @BeforeEach
    @Transactional
    void setup() {
        if (!userRepository.existsByEmail(EMAIL)) {
            slug = authService.register(new RegisterRequest(
                    "Colegio HTTP Test", "Pedro", "Martínez", EMAIL, PASS, null, "SEMESTER"
            ));
            activateUser();
        } else {
            // El tenant ya existe — recuperar el slug desde la BD
            var ut = userTenantRepository.findAllByUser_Email(EMAIL).stream()
                    .findFirst().orElseThrow();
            slug = tenantRepository.findById(ut.getTenantId())
                    .map(t -> t.getSlug())
                    .orElseThrow();
        }
    }

    @Test
    @DisplayName("should_return_201_when_register_is_valid")
    void should_return_201_when_register_is_valid() throws Exception {
        var request = new RegisterRequest(
                "Colegio HTTP Nuevo", "María", "García", "admin@nuevo-http.mx", "Password123!", "Una descripción", "TRIMESTER"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(containsString("correo")))
                .andExpect(jsonPath("$.slug").isNotEmpty());
    }

    @Test
    @DisplayName("should_return_400_when_register_has_invalid_fields")
    void should_return_400_when_register_has_invalid_fields() throws Exception {
        var request = new RegisterRequest("", "", "", "not-an-email", "short", "x".repeat(501), "INVALID");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    @DisplayName("should_return_tenants_when_validate_with_valid_credentials")
    void should_return_tenants_when_validate_with_valid_credentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValidateRequest(EMAIL, PASS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.tenants", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.tenants[0].slug").value(slug));
    }

    @Test
    @DisplayName("should_return_409_when_validate_with_wrong_password")
    void should_return_409_when_validate_with_wrong_password() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValidateRequest(EMAIL, "wrong"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("should_return_200_with_tokens_when_login_is_valid")
    void should_return_200_with_tokens_when_login_is_valid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(EMAIL, PASS, slug, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(EMAIL))
                .andExpect(jsonPath("$.user.roleName").value("ADMIN"));
    }

    @Test
    @DisplayName("should_return_401_when_login_with_wrong_password")
    void should_return_401_when_login_with_wrong_password() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(EMAIL, "wrongpass", slug, null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should_return_user_info_when_authenticated")
    void should_return_user_info_when_authenticated() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    @DisplayName("should_return_401_when_me_without_token")
    void should_return_401_when_me_without_token() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should_return_permissions_list_when_authenticated")
    void should_return_permissions_list_when_authenticated() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/v1/auth/me/permissions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("should_return_my_tenants_when_authenticated")
    void should_return_my_tenants_when_authenticated() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/v1/auth/me/tenants")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].slug").value(slug));
    }

    @Test
    @DisplayName("should_return_new_tokens_when_refresh_is_valid")
    void should_return_new_tokens_when_refresh_is_valid() throws Exception {
        var loginRes = authService.login(new LoginRequest(EMAIL, PASS, slug, null));
        String refreshToken = loginRes.refreshToken();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("should_return_200_when_logout_with_valid_token")
    void should_return_200_when_logout_with_valid_token() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("cerrada")));
    }

    private String loginAndGetToken() {
        return authService.login(new LoginRequest(EMAIL, PASS, slug, null)).accessToken();
    }

    @Transactional
    void activateUser() {
        var tenant = tenantRepository.findBySlug(slug).orElseThrow();
        userRepository.findByEmail(EMAIL).ifPresent(u -> {
            u.verifyEmail();
            userRepository.save(u);
        });
        userTenantRepository.findByUser_EmailAndTenantId(EMAIL, tenant.getId()).ifPresent(ut -> {
            ut.activate();
            userTenantRepository.save(ut);
        });
    }
}

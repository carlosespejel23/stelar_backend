package com.education.stelar.kernel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.education.stelar.kernel.security.UserPrincipal;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            if (authentication.getPrincipal() instanceof UserPrincipal principal) {
                return Optional.of(principal.getId());
            }
            return Optional.empty();
        };
    }
}

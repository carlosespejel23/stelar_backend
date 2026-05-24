package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.entity.UserTenant;
import com.education.stelar.identity.repository.UserTenantRepository;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserTenantRepository userTenantRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UUID tenantId = TenantContext.getCurrentTenant();

        UserTenant userTenant = userTenantRepository.findByUser_EmailAndTenantId(email, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + email + " en tenant: " + tenantId));

        return UserDetailsImpl.build(userTenant.getUser(), userTenant);
    }
}
